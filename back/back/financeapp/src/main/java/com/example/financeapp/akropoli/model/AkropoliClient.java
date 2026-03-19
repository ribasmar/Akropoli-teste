package com.example.financeapp.akropoli.model;

import com.example.financeapp.akropoli.config.AkropoliProperties;
import com.example.financeapp.akropoli.dto.AkropoliDto;
import com.example.financeapp.exception.AkropoliIntegrationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AkropoliClient {

    private final RestClient akropoliRestClient;
    private final AkropoliProperties properties;
    private final StringRedisTemplate redisTemplate;

    private static final String REDIS_TOKEN_KEY = "akropoli:access_token";
    private static final String REDIS_LOCK_KEY = "akropoli:token_lock";
    private static final long EXPIRY_MARGIN_SECONDS = 60L;

    // ── Autenticação e Redis ──────────────────────────────────────────────────

    public String getValidToken() {
        String token = redisTemplate.opsForValue().get(REDIS_TOKEN_KEY);
        if (token != null) {
            return token;
        }

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(REDIS_LOCK_KEY, "locked", Duration.ofSeconds(15));

        if (Boolean.TRUE.equals(acquired)) {
            try {
                return refreshToken();
            } finally {
                redisTemplate.delete(REDIS_LOCK_KEY);
            }
        } else {
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            String newToken = redisTemplate.opsForValue().get(REDIS_TOKEN_KEY);
            if (newToken != null) return newToken;
            throw new RuntimeException("Timeout a aguardar renovação de token por outra instância.");
        }
    }

    private String refreshToken() {
        log.debug("Refreshing Akropoli access token via RestClient");
        try {
            AkropoliDto.TokenResponse response = akropoliRestClient.post()
                    .uri("/v1/token/client-token-generation/")
                    .body(new AkropoliDto.TokenRequest(
                            properties.getClientId(),
                            properties.getClientSecret()))
                    .retrieve()
                    .body(AkropoliDto.TokenResponse.class);

            if (response == null || response.getAccessToken() == null) {
                throw AkropoliIntegrationException.authenticationFailed(
                        new RuntimeException("Empty access_token in response"));
            }

            long ttl = response.getExpiresIn() != null ? response.getExpiresIn() : properties.getTokenTtlSeconds();
            long safeTtl = Math.max(1, ttl - EXPIRY_MARGIN_SECONDS);

            redisTemplate.opsForValue().set(REDIS_TOKEN_KEY, response.getAccessToken(), Duration.ofSeconds(safeTtl));
            return response.getAccessToken();

        } catch (Exception e) {
            log.error("Akropoli authentication failed: {}", e.getMessage());
            throw AkropoliIntegrationException.authenticationFailed(e);
        }
    }

    // ── Resources ─────────────────────────────────────────────────────────────

    public List<AkropoliDto.Resource> getResources(String linkId) {
        log.debug("Fetching resources for linkId: {}", linkId);
        return fetchList("/v1/resources/links/{linkId}/data/resources",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<List<AkropoliDto.Resource>>>() {}, linkId);
    }

    // ── Accounts ──────────────────────────────────────────────────────────────

    public List<AkropoliDto.Account> getAccounts(String linkId) {
        return fetchList("/v1/accounts/links/{linkId}/data/accounts",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<List<AkropoliDto.Account>>>() {}, linkId);
    }

    public AkropoliDto.AccountBalance getAccountBalance(String linkId, String accountId) {
        return fetchSingle("/v1/accounts/links/{linkId}/data/accounts/{accountId}/balances",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<AkropoliDto.AccountBalance>>() {}, linkId, accountId);
    }

    public List<AkropoliDto.AccountTransaction> getAccountTransactions(String linkId, String accountId) {
        return fetchList("/v1/accounts/links/{linkId}/data/accounts/{accountId}/transactions",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<List<AkropoliDto.AccountTransaction>>>() {}, linkId, accountId);
    }

    public List<AkropoliDto.AccountTransaction> getAccountTransactionsCurrent(String linkId, String accountId) {
        return fetchList("/v1/accounts/links/{linkId}/data/accounts/{accountId}/transactions-current",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<List<AkropoliDto.AccountTransaction>>>() {}, linkId, accountId);
    }

    // ── Credit Cards ──────────────────────────────────────────────────────────

    public List<AkropoliDto.CreditCard> getCreditCards(String linkId) {
        return fetchList("/v1/credit-cards/links/{linkId}/data/credit-cards",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<List<AkropoliDto.CreditCard>>>() {}, linkId);
    }

    public List<AkropoliDto.CreditCardLimit> getCreditCardLimits(String linkId, String creditCardId) {
        return fetchList("/v1/credit-cards/links/{linkId}/data/credit-cards/{creditCardId}/limits",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<List<AkropoliDto.CreditCardLimit>>>() {}, linkId, creditCardId);
    }

    public List<AkropoliDto.CreditCardBill> getCreditCardBills(String linkId, String creditCardId) {
        return fetchList("/v1/credit-cards/links/{linkId}/data/credit-cards/{creditCardId}/bills",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<List<AkropoliDto.CreditCardBill>>>() {}, linkId, creditCardId);
    }

    public List<AkropoliDto.CreditCardTransaction> getCreditCardTransactions(String linkId, String creditCardId) {
        return fetchList("/v1/credit-cards/links/{linkId}/data/credit-cards/{creditCardId}/transactions",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<List<AkropoliDto.CreditCardTransaction>>>() {}, linkId, creditCardId);
    }

    public List<AkropoliDto.CreditCardTransaction> getCreditCardBillTransactions(String linkId, String creditCardId, String billId) {
        return fetchList("/v1/credit-cards/links/{linkId}/data/credit-cards/{creditCardId}/bills/{billId}/transactions",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<List<AkropoliDto.CreditCardTransaction>>>() {}, linkId, creditCardId, billId);
    }

    // ── Loans ─────────────────────────────────────────────────────────────────

    public List<AkropoliDto.Loan> getLoans(String linkId) {
        return fetchList("/v1/loans/links/{linkId}/data/loans",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<List<AkropoliDto.Loan>>>() {}, linkId);
    }

    public AkropoliDto.LoanDetail getLoanDetail(String linkId, String contractId) {
        return fetchSingle("/v1/loans/links/{linkId}/data/loans/{contractId}",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<AkropoliDto.LoanDetail>>() {}, linkId, contractId);
    }

    public List<AkropoliDto.LoanWarranty> getLoanWarranties(String linkId, String contractId) {
        return fetchList("/v1/loans/links/{linkId}/data/loans/{contractId}/warranties",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<List<AkropoliDto.LoanWarranty>>>() {}, linkId, contractId);
    }

    public AkropoliDto.LoanPayments getLoanPayments(String linkId, String contractId) {
        return fetchSingle("/v1/loans/links/{linkId}/data/loans/{contractId}/payments",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<AkropoliDto.LoanPayments>>() {}, linkId, contractId);
    }

    public AkropoliDto.LoanScheduledInstalments getLoanScheduledInstalments(String linkId, String contractId) {
        return fetchSingle("/v1/loans/links/{linkId}/data/loans/{contractId}/scheduled-instalments",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<AkropoliDto.LoanScheduledInstalments>>() {}, linkId, contractId);
    }

    // ── Financings ────────────────────────────────────────────────────────────

    public List<AkropoliDto.Financing> getFinancings(String linkId) {
        return fetchList("/v1/financings/links/{linkId}/data/financings",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<List<AkropoliDto.Financing>>>() {}, linkId);
    }

    public List<AkropoliDto.InvoiceFinancing> getInvoiceFinancings(String linkId) {
        return fetchList("/v1/invoice-financings/links/{linkId}/data/invoice-financings",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<List<AkropoliDto.InvoiceFinancing>>>() {}, linkId);
    }

    // ── Investments ───────────────────────────────────────────────────────────

    public List<AkropoliDto.InvestmentFund> getInvestmentFunds(String linkId) {
        return fetchList("/v1/investments/links/{linkId}/data/investments/funds",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<List<AkropoliDto.InvestmentFund>>>() {}, linkId);
    }

    public AkropoliDto.InvestmentFundBalance getInvestmentFundBalance(String linkId, String investmentId) {
        return fetchSingle("/v1/investments/links/{linkId}/data/investments/funds/{investmentId}/balances",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<AkropoliDto.InvestmentFundBalance>>() {}, linkId, investmentId);
    }

    public List<AkropoliDto.BankFixedIncome> getBankFixedIncomes(String linkId) {
        return fetchList("/v1/investments/links/{linkId}/data/investments/bank-fixed-incomes",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<List<AkropoliDto.BankFixedIncome>>>() {}, linkId);
    }

    public AkropoliDto.BankFixedIncomeBalance getBankFixedIncomeBalance(String linkId, String investmentId) {
        return fetchSingle("/v1/investments/links/{linkId}/data/investments/bank-fixed-incomes/{investmentId}/balances",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<AkropoliDto.BankFixedIncomeBalance>>() {}, linkId, investmentId);
    }

    // ── Customers ─────────────────────────────────────────────────────────────

    public AkropoliDto.CustomerPersonal getCustomerPersonal(String linkId) {
        return fetchSingle("/v1/customers/links/{linkId}/data/identifications/personal",
                new ParameterizedTypeReference<AkropoliDto.ApiResponse<AkropoliDto.CustomerPersonal>>() {}, linkId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private <T> List<T> fetchList(String uriTemplate, ParameterizedTypeReference<AkropoliDto.ApiResponse<List<T>>> type, Object... uriVars) {
        String token = getValidToken();
        try {
            AkropoliDto.ApiResponse<List<T>> response = akropoliRestClient.get()
                    .uri(uriTemplate, uriVars)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(type);

            return (response != null && response.getData() != null) ? response.getData() : List.of();

        } catch (HttpClientErrorException.Forbidden e) {
            log.warn("Akropoli 403 for {}: resource not in consent scope", uriTemplate);
            return List.of();
        } catch (Exception e) {
            log.error("Failed to fetch list from {}: {}", uriTemplate, e.getMessage());
            throw AkropoliIntegrationException.fetchFailed(uriTemplate, e);
        }
    }

    private <T> T fetchSingle(String uriTemplate, ParameterizedTypeReference<AkropoliDto.ApiResponse<T>> type, Object... uriVars) {
        String token = getValidToken();
        try {
            AkropoliDto.ApiResponse<T> response = akropoliRestClient.get()
                    .uri(uriTemplate, uriVars)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(type);

            if (response == null || response.getData() == null) {
                throw AkropoliIntegrationException.fetchFailed(uriTemplate, new RuntimeException("Empty data in response"));
            }
            return response.getData();

        } catch (HttpClientErrorException.Forbidden e) {
            log.warn("Akropoli 403 for {}: resource not in consent scope", uriTemplate);
            throw AkropoliIntegrationException.consentScopeMissing(uriTemplate);
        } catch (Exception e) {
            log.error("Failed to fetch single resource from {}: {}", uriTemplate, e.getMessage());
            throw AkropoliIntegrationException.fetchFailed(uriTemplate, e);
        }
    }
}