package com.example.financeapp.akropoli.service;

import com.example.financeapp.akropoli.model.AkropoliClient;
import com.example.financeapp.akropoli.dto.AkropoliDto;
import com.example.financeapp.client.model.Client;
import com.example.financeapp.client.model.Client.ConnectionStatus;
import com.example.financeapp.client.model.ClientAnalytics;
import com.example.financeapp.client.repository.ClientAnalyticsRepository;
import com.example.financeapp.client.repository.ClientRepository;
import com.example.financeapp.exception.AkropoliIntegrationException;
import com.example.financeapp.exception.ClientNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service de integração Akropoli Open Finance.
 *
 * Fluxo de onboarding:
 *   1. Banker solicita link de consentimento → Akropoli gera URL
 *   2. URL é enviada ao cliente final (WhatsApp, email, etc.)
 *   3. Cliente concede consentimento via jornada web da Akropoli
 *   4. Akropoli dispara webhook CONSENT_AUTHORISED com linkId
 *   5. handleConsentAuthorised() é chamado pelo WebhookController
 *   6. syncClientData() busca os dados e atualiza analytics
 *
 * Escopo de consentimento:
 *   Antes de cada sync, getResources() verifica quais dados o usuário autorizou.
 *   Endpoints fora do escopo retornam lista vazia (403 tratado no AkropoliClient).
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AkropoliService {

    private final AkropoliClient akropoliClient;
    private final ClientRepository clientRepository;
    private final ClientAnalyticsRepository analyticsRepository;

    // ── Webhook handlers ──────────────────────────────────────────────────────

    /**
     * Chamado pelo WebhookController quando Akropoli envia CONSENT_AUTHORISED.
     * Associa o linkId ao cliente e inicia a primeira sincronização.
     *
     * Idempotente: se o cliente já tiver o mesmo linkId, apenas re-sincroniza.
     * Se tiver um linkId diferente, rejeita — é necessário revogar o anterior.
     */
    public void handleConsentAuthorised(String linkId, String mongoClientId) {
        log.info("Consent authorised: linkId={} clientId={}", linkId, mongoClientId);

        Client client = clientRepository.findById(mongoClientId)
                .orElseThrow(() -> new ClientNotFoundException(mongoClientId));

        if (client.getAkropoliLinkId() != null && !client.getAkropoliLinkId().equals(linkId)) {
            log.warn("Client {} already has linkId {} — rejecting new linkId {}",
                    mongoClientId, client.getAkropoliLinkId(), linkId);
            throw new IllegalStateException(
                    "Client already has an active consent. Revoke it before associating a new one.");
        }

        client.setAkropoliLinkId(linkId);
        client.setConnectionStatus(ConnectionStatus.UPDATING);
        // Consentimentos Open Finance expiram em no máximo 12 meses (BACEN)
        client.setConsentExpiresAt(LocalDateTime.now().plusMonths(12));
        clientRepository.save(client);

        syncClientData(client);
    }

    /**
     * Chamado pelo WebhookController quando Akropoli envia DATA_AVAILABLE.
     * Atualiza dados de um cliente já conectado.
     */
    public void syncByLinkId(String linkId) {
        log.info("Webhook sync triggered for linkId: {}", linkId);

        Client client = clientRepository.findByAkropoliLinkId(linkId)
                .orElseThrow(() -> {
                    log.warn("No client found for linkId: {} — webhook ignored", linkId);
                    return new ClientNotFoundException("linkId=" + linkId);
                });

        syncClientData(client);
    }

    /**
     * Chamado pelo WebhookController quando Akropoli envia CONSENT_EXPIRED ou CONSENT_REVOKED.
     */
    public void markConsentExpired(String linkId) {
        log.warn("Marking CONSENT_EXPIRED for linkId: {}", linkId);
        updateStatusByLinkId(linkId, ConnectionStatus.CONSENT_EXPIRED);
    }

    public void markConsentRevoked(String linkId) {
        log.warn("Marking CONSENT_REVOKED for linkId: {}", linkId);
        updateStatusByLinkId(linkId, ConnectionStatus.CONSENT_REVOKED);
    }

    public void markConnectionError(String linkId) {
        log.warn("Marking CONNECTION_ERROR for linkId: {}", linkId);
        updateStatusByLinkId(linkId, ConnectionStatus.CONNECTION_ERROR);
    }

    // ── Sincronização ─────────────────────────────────────────────────────────

    public void syncClientData(String clientId, String bankerId) {
        syncClientData(findClient(clientId, bankerId));
    }

    public void syncClientData(Client client) {
        validateLinkId(client);

        String linkId = client.getAkropoliLinkId();
        log.info("Starting Akropoli data sync for client: {} linkId: {}", client.getId(), linkId);

        try {
            client.setConnectionStatus(ConnectionStatus.UPDATING);
            clientRepository.save(client);

            // 1. Verificar escopo do consentimento antes de buscar dados
            List<AkropoliDto.Resource> resources = akropoliClient.getResources(linkId);
            log.debug("Available resources for client {}: {}", client.getId(),
                    resources.stream().map(AkropoliDto.Resource::getType).toList());

            // 2. Contas e saldos
            List<AkropoliDto.Account> accounts = akropoliClient.getAccounts(linkId);
            List<AkropoliDto.AccountBalance> balances = accounts.stream()
                    .map(a -> akropoliClient.getAccountBalance(linkId, a.getAccountId()))
                    .toList();

            // 3. Transações (histórico completo de cada conta)
            List<AkropoliDto.AccountTransaction> transactions = accounts.stream()
                    .flatMap(a -> akropoliClient
                            .getAccountTransactions(linkId, a.getAccountId())
                            .stream())
                    .toList();

            // 4. Cartões de crédito (se no escopo)
            List<AkropoliDto.CreditCard> creditCards = hasResource(resources, "CREDIT_CARD")
                    ? akropoliClient.getCreditCards(linkId)
                    : List.of();

            List<AkropoliDto.CreditCardLimit> creditCardLimits = creditCards.stream()
                    .flatMap(c -> akropoliClient
                            .getCreditCardLimits(linkId, c.getCreditCardAccountId())
                            .stream())
                    .toList();

            // 5. Crédito: empréstimos e financiamentos (se no escopo)
            List<AkropoliDto.LoanPayments> loanPayments = List.of();
            List<AkropoliDto.LoanScheduledInstalments> loanInstalments = List.of();

            if (hasResource(resources, "LOAN")) {
                List<AkropoliDto.Loan> loans = akropoliClient.getLoans(linkId);
                loanPayments = loans.stream()
                        .map(l -> akropoliClient.getLoanPayments(linkId, l.getContractId()))
                        .toList();
                loanInstalments = loans.stream()
                        .map(l -> akropoliClient.getLoanScheduledInstalments(linkId, l.getContractId()))
                        .toList();
            }

            // 6. Investimentos (se no escopo)
            List<AkropoliDto.InvestmentFundBalance> fundBalances = List.of();
            List<AkropoliDto.BankFixedIncomeBalance> fixedIncomeBalances = List.of();

            if (hasResource(resources, "INVESTMENT")) {
                fundBalances = akropoliClient.getInvestmentFunds(linkId).stream()
                        .map(f -> akropoliClient.getInvestmentFundBalance(linkId, f.getInvestmentId()))
                        .toList();
                fixedIncomeBalances = akropoliClient.getBankFixedIncomes(linkId).stream()
                        .map(f -> akropoliClient.getBankFixedIncomeBalance(linkId, f.getInvestmentId()))
                        .toList();
            }

            // 7. Atualizar analytics
            updateAnalytics(client.getId(), balances, transactions,
                    creditCardLimits, loanPayments, loanInstalments,
                    fundBalances, fixedIncomeBalances);

            client.setConnectionStatus(ConnectionStatus.UPDATED);
            client.setLastSync(LocalDateTime.now());
            clientRepository.save(client);

            log.info("Akropoli sync completed for client: {}", client.getId());

        } catch (AkropoliIntegrationException e) {
            log.error("Sync failed for client {}: {}", client.getId(), e.getMessage());
            client.setConnectionStatus(ConnectionStatus.CONNECTION_ERROR);
            clientRepository.save(client);
            throw e;
        }
    }

    // ── Consultas diretas ─────────────────────────────────────────────────────

    public List<AkropoliDto.Account> getAccounts(String clientId, String bankerId) {
        Client client = findClient(clientId, bankerId);
        validateLinkId(client);
        return akropoliClient.getAccounts(client.getAkropoliLinkId());
    }

    public List<AkropoliDto.AccountTransaction> getTransactions(String clientId, String bankerId) {
        Client client = findClient(clientId, bankerId);
        validateLinkId(client);

        String linkId = client.getAkropoliLinkId();
        return akropoliClient.getAccounts(linkId).stream()
                .flatMap(a -> akropoliClient
                        .getAccountTransactionsCurrent(linkId, a.getAccountId())
                        .stream())
                .toList();
    }

    public List<AkropoliDto.InvestmentFund> getInvestmentFunds(String clientId, String bankerId) {
        Client client = findClient(clientId, bankerId);
        validateLinkId(client);
        return akropoliClient.getInvestmentFunds(client.getAkropoliLinkId());
    }

    public List<AkropoliDto.CreditCard> getCreditCards(String clientId, String bankerId) {
        Client client = findClient(clientId, bankerId);
        validateLinkId(client);
        return akropoliClient.getCreditCards(client.getAkropoliLinkId());
    }

    public List<AkropoliDto.Loan> getLoans(String clientId, String bankerId) {
        Client client = findClient(clientId, bankerId);
        validateLinkId(client);
        return akropoliClient.getLoans(client.getAkropoliLinkId());
    }

    public List<AkropoliDto.Resource> getResources(String clientId, String bankerId) {
        Client client = findClient(clientId, bankerId);
        validateLinkId(client);
        return akropoliClient.getResources(client.getAkropoliLinkId());
    }

    // ── Analytics ─────────────────────────────────────────────────────────────

    private void updateAnalytics(
            String mongoClientId,
            List<AkropoliDto.AccountBalance> balances,
            List<AkropoliDto.AccountTransaction> transactions,
            List<AkropoliDto.CreditCardLimit> creditCardLimits,
            List<AkropoliDto.LoanPayments> loanPayments,
            List<AkropoliDto.LoanScheduledInstalments> loanInstalments,
            List<AkropoliDto.InvestmentFundBalance> fundBalances,
            List<AkropoliDto.BankFixedIncomeBalance> fixedIncomeBalances) {

        // ── Saldo disponível consolidado ──────────────────────────────────────
        BigDecimal currentBalance = balances.stream()
                .filter(b -> b.getAvailableAmount() != null)
                .map(b -> b.getAvailableAmount().toBigDecimal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── Receitas e despesas das transações de conta corrente ──────────────
        BigDecimal totalIncome = transactions.stream()
                .map(AkropoliDto.AccountTransaction::getSignedAmount)
                .filter(a -> a.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .map(AkropoliDto.AccountTransaction::getSignedAmount)
                .filter(a -> a.compareTo(BigDecimal.ZERO) < 0)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Divisão por 3 meses (janela padrão de sync)
        BigDecimal avgMonthlyIncome = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? totalIncome.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal avgMonthlyExpenses = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                ? totalExpenses.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // ── Saldo devedor de crédito ──────────────────────────────────────────
        BigDecimal totalDebtBalance = loanPayments.stream()
                .filter(p -> p.getContractOutstandingBalance() != null)
                .map(p -> p.getContractOutstandingBalance().toBigDecimal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── Parcela mensal de crédito (soma das parcelas em aberto) ──────────
        BigDecimal monthlyDebtInstalment = loanInstalments.stream()
                .filter(i -> i.getDueInstalments() != null
                        && i.getTotalNumberOfInstalments() != null
                        && i.getTotalNumberOfInstalments() > 0)
                .map(i -> BigDecimal.valueOf(i.getDueInstalments()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── Comprometimento de renda (parcela / receita média mensal) ─────────
        BigDecimal debtToIncomeRatio = BigDecimal.ZERO;
        if (avgMonthlyIncome.compareTo(BigDecimal.ZERO) > 0
                && monthlyDebtInstalment.compareTo(BigDecimal.ZERO) > 0) {
            debtToIncomeRatio = monthlyDebtInstalment
                    .divide(avgMonthlyIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // ── Uso de limite de cartão de crédito ────────────────────────────────
        BigDecimal totalCreditLimit = creditCardLimits.stream()
                .filter(c -> c.getLimitAmount() != null)
                .map(AkropoliDto.CreditCardLimit::getLimitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCreditUsed = creditCardLimits.stream()
                .filter(c -> c.getUsedAmount() != null)
                .map(AkropoliDto.CreditCardLimit::getUsedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal creditCardUsageRate = BigDecimal.ZERO;
        if (totalCreditLimit.compareTo(BigDecimal.ZERO) > 0) {
            creditCardUsageRate = totalCreditUsed
                    .divide(totalCreditLimit, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // ── Maior categoria de gasto ──────────────────────────────────────────
        String topCategory = transactions.stream()
                .filter(t -> t.getSignedAmount().compareTo(BigDecimal.ZERO) < 0)
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionName() != null ? t.getTransactionName() : "Outros",
                        Collectors.reducing(BigDecimal.ZERO,
                                t -> t.getSignedAmount().abs(),
                                BigDecimal::add)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        // ── Savings rate ──────────────────────────────────────────────────────
        BigDecimal savingsRate = BigDecimal.ZERO;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = totalIncome.subtract(totalExpenses)
                    .divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // ── Projeção de saldo ─────────────────────────────────────────────────
        BigDecimal projectedBalance = currentBalance
                .add(avgMonthlyIncome.subtract(avgMonthlyExpenses));

        // ── Health score composto (5 indicadores ponderados) ──────────────────

        // I1 — Savings rate (peso 25%): 0% = 0pts, 50%+ = 100pts
        BigDecimal savingsScore = savingsRate
                .divide(BigDecimal.valueOf(50), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .min(BigDecimal.valueOf(100)).max(BigDecimal.ZERO);

        // I2 — Projeção de saldo próximos 30 dias (peso 20%)
        BigDecimal projectionScore;
        if (projectedBalance.compareTo(BigDecimal.ZERO) >= 0) {
            projectionScore = BigDecimal.valueOf(100);
        } else if (currentBalance.compareTo(BigDecimal.ZERO) > 0) {
            projectionScore = projectedBalance
                    .divide(currentBalance, 4, RoundingMode.HALF_UP)
                    .add(BigDecimal.ONE)
                    .multiply(BigDecimal.valueOf(100))
                    .max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));
        } else {
            projectionScore = BigDecimal.ZERO;
        }

        // I3 — Expense ratio: despesas/receita (peso 20%)
        BigDecimal expenseScore = BigDecimal.ZERO;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            expenseScore = BigDecimal.ONE
                    .subtract(totalExpenses.divide(totalIncome, 4, RoundingMode.HALF_UP))
                    .multiply(BigDecimal.valueOf(100))
                    .max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));
        }

        // I4 — Presença de investimentos (peso 20%)
        boolean hasInvestments = !fundBalances.isEmpty() || !fixedIncomeBalances.isEmpty();
        BigDecimal investmentScore = hasInvestments ? BigDecimal.valueOf(100) : BigDecimal.ZERO;

        // I5 — Comprometimento de renda (peso 15%): 0% = 100pts, 50%+ = 0pts
        BigDecimal debtScore = debtToIncomeRatio.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.valueOf(100)
                : BigDecimal.ONE
                .subtract(debtToIncomeRatio.divide(BigDecimal.valueOf(50), 4, RoundingMode.HALF_UP))
                .multiply(BigDecimal.valueOf(100))
                .max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));

        BigDecimal healthScore = savingsScore   .multiply(BigDecimal.valueOf(0.25))
                .add(projectionScore .multiply(BigDecimal.valueOf(0.20)))
                .add(expenseScore    .multiply(BigDecimal.valueOf(0.20)))
                .add(investmentScore .multiply(BigDecimal.valueOf(0.20)))
                .add(debtScore       .multiply(BigDecimal.valueOf(0.15)))
                .setScale(2, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));

        // ── Upsert analytics ──────────────────────────────────────────────────
        ClientAnalytics analytics = analyticsRepository
                .findByMongoClientId(mongoClientId)
                .orElse(ClientAnalytics.builder().mongoClientId(mongoClientId).build());

        analytics.setCurrentBalance(currentBalance);
        analytics.setProjectedBalance(projectedBalance);
        analytics.setAvgMonthlyIncome(avgMonthlyIncome);
        analytics.setAvgMonthlyExpenses(avgMonthlyExpenses);
        analytics.setSavingsRate(savingsRate);
        analytics.setFinancialHealthScore(healthScore);
        analytics.setTopSpendingCategory(topCategory);

        analyticsRepository.save(analytics);
        log.info("Analytics updated for client: {} (healthScore={} debtRatio={}%)",
                mongoClientId, healthScore, debtToIncomeRatio);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Client findClient(String clientId, String bankerId) {
        return clientRepository.findByIdAndBankerId(clientId, bankerId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
    }

    private void validateLinkId(Client client) {
        if (client.getAkropoliLinkId() == null || client.getAkropoliLinkId().isBlank()) {
            throw AkropoliIntegrationException.linkIdNotFound(client.getId());
        }
        if (client.getConnectionStatus() == ConnectionStatus.CONSENT_EXPIRED) {
            throw AkropoliIntegrationException.consentExpired(client.getId());
        }
    }

    private boolean hasResource(List<AkropoliDto.Resource> resources, String type) {
        return resources.stream()
                .anyMatch(r -> type.equalsIgnoreCase(r.getType())
                        && "AVAILABLE".equalsIgnoreCase(r.getStatus()));
    }

    private void updateStatusByLinkId(String linkId, ConnectionStatus status) {
        clientRepository.findByAkropoliLinkId(linkId).ifPresentOrElse(
                client -> {
                    client.setConnectionStatus(status);
                    clientRepository.save(client);
                    log.info("Client {} status → {}", client.getId(), status);
                },
                () -> log.warn("No client found for linkId: {} — webhook ignored", linkId));
    }
}