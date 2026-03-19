package com.example.financeapp.akropoli.controller;

import com.example.financeapp.akropoli.dto.AkropoliDto;
import com.example.financeapp.akropoli.service.AkropoliService;
import com.example.financeapp.auth.model.Banker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para a integração Akropoli Open Finance.
 */
@RestController
@RequestMapping("/api/v1/clients/{clientId}/akropoli")
@RequiredArgsConstructor
public class AkropoliController {

    private final AkropoliService akropoliService;

    /**
     * Lista contas bancárias e saldos do cliente.
     * GET /api/v1/clients/{clientId}/akropoli/accounts
     */
    @GetMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AkropoliDto.Account>> getAccounts(
            @PathVariable String clientId,
            @AuthenticationPrincipal Banker banker) {

        return ResponseEntity.ok(akropoliService.getAccounts(clientId, banker.getId()));
    }

    /**
     * Lista transações do período corrente (últimos 7 dias, padrão BACEN).
     * Para histórico completo, use o endpoint de sync e consulte o banco local.
     * GET /api/v1/clients/{clientId}/akropoli/transactions
     */
    @GetMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AkropoliDto.AccountTransaction>> getTransactions(
            @PathVariable String clientId,
            @AuthenticationPrincipal Banker banker) {

        return ResponseEntity.ok(akropoliService.getTransactions(clientId, banker.getId()));
    }

    /**
     * Lista fundos de investimento do cliente.
     * GET /api/v1/clients/{clientId}/akropoli/investments/funds
     */
    @GetMapping(value = "/investments/funds", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AkropoliDto.InvestmentFund>> getInvestmentFunds(
            @PathVariable String clientId,
            @AuthenticationPrincipal Banker banker) {

        return ResponseEntity.ok(akropoliService.getInvestmentFunds(clientId, banker.getId()));
    }

    /**
     * Lista cartões de crédito com limites da integração Akropoli.
     * GET /api/v1/clients/{clientId}/akropoli/credit-cards
     */
    @GetMapping(value = "/credit-cards", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AkropoliDto.CreditCard>> getCreditCards(
            @PathVariable String clientId,
            @AuthenticationPrincipal Banker banker) {

        return ResponseEntity.ok(akropoliService.getCreditCards(clientId, banker.getId()));
    }

    /**
     * Lista empréstimos e financiamentos da integração Akropoli.
     * GET /api/v1/clients/{clientId}/akropoli/loans
     */
    @GetMapping(value = "/loans", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AkropoliDto.Loan>> getLoans(
            @PathVariable String clientId,
            @AuthenticationPrincipal Banker banker) {

        return ResponseEntity.ok(akropoliService.getLoans(clientId, banker.getId()));
    }

    /**
     * Lista os recursos autorizados no consentimento do cliente.
     * Útil para o frontend saber quais seções exibir.
     * GET /api/v1/clients/{clientId}/akropoli/resources
     */
    @GetMapping(value = "/resources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AkropoliDto.Resource>> getResources(
            @PathVariable String clientId,
            @AuthenticationPrincipal Banker banker) {

        return ResponseEntity.ok(akropoliService.getResources(clientId, banker.getId()));
    }

    /**
     * Força re-sincronização manual dos dados do cliente.
     * POST /api/v1/clients/{clientId}/akropoli/sync
     */
    @PostMapping("/sync")
    public ResponseEntity<Void> sync(
            @PathVariable String clientId,
            @AuthenticationPrincipal Banker banker) {

        akropoliService.syncClientData(clientId, banker.getId());
        return ResponseEntity.ok().build();
    }
}