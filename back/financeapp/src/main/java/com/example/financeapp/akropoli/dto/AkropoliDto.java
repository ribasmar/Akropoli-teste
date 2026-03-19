package com.example.financeapp.akropoli.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTOs para a API Akropoli Open Finance Data v0.1.0 (OAS 3.1 / FastAPI).
 *
 * Padrão de resposta da API:
 *   Todos os endpoints retornam envelope { "data": {...}, "meta": {...} }.
 *   A classe genérica ApiResponse<T> captura esse envelope.
 *
 * Valores monetários:
 *   A Akropoli segue o padrão BACEN — valores são objetos AmountDetail
 *   com campos "amount" (String com precisão) e "currency" (ISO 4217).
 *   Convertemos para BigDecimal nos mappers do service.
 *
 * IDs:
 *   O link_id e demais IDs são Strings (não UUIDs) no padrão Open Finance Brasil.
 */
public class AkropoliDto {

    // ── Auth / Token ──────────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenRequest {
        @JsonProperty("client_id")
        private String clientId;

        @JsonProperty("client_secret")
        private String clientSecret;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenResponse {
        /** Bearer token para uso nos headers Authorization das chamadas de dados. */
        @JsonProperty("access_token")
        private String accessToken;

        /** Tipo do token — normalmente "bearer". */
        @JsonProperty("token_type")
        private String tokenType;

        /** Expiração em segundos a partir da emissão. */
        @JsonProperty("expires_in")
        private Long expiresIn;
    }

    // ── Envelope genérico ─────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiResponse<T> {
        @JsonProperty("data")
        private T data;

        @JsonProperty("meta")
        private Meta meta;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        @JsonProperty("totalRecords")
        private Integer totalRecords;

        @JsonProperty("totalPages")
        private Integer totalPages;

        @JsonProperty("requestDateTime")
        private String requestDateTime;
    }

    // ── Valor monetário (padrão BACEN) ────────────────────────────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AmountDetail {
        /** Valor como string com precisão total (ex: "1234.56"). */
        @JsonProperty("amount")
        private String amount;

        /** Código ISO 4217 (ex: "BRL"). */
        @JsonProperty("currency")
        private String currency;

        /** Converte para BigDecimal; retorna ZERO se nulo ou inválido. */
        public BigDecimal toBigDecimal() {
            if (amount == null || amount.isBlank()) return BigDecimal.ZERO;
            try { return new BigDecimal(amount); }
            catch (NumberFormatException e) { return BigDecimal.ZERO; }
        }
    }

    // ── Resources ─────────────────────────────────────────────────────────────

    /**
     * Retornado por GET /v1/resources/links/{link_id}/data/resources.
     * Lista os tipos de recursos disponíveis no consentimento do usuário.
     * Consultar ANTES de chamar outros endpoints para verificar escopo.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Resource {
        /** Tipo do recurso: ACCOUNT, CREDIT_CARD, LOAN, FINANCING, INVESTMENT, CUSTOMER. */
        @JsonProperty("type")
        private String type;

        /** Status: AVAILABLE, PENDING, TEMPORARILY_UNAVAILABLE. */
        @JsonProperty("status")
        private String status;

        /** ID específico do recurso dentro do consentimento. */
        @JsonProperty("resourceId")
        private String resourceId;
    }

    // ── Accounts ──────────────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Account {
        @JsonProperty("brandName")
        private String brandName;

        @JsonProperty("companyCnpj")
        private String companyCnpj;

        /** CONTA_DEPOSITO_A_VISTA, CONTA_POUPANCA, CONTA_PAGAMENTO_PRE_PAGA. */
        @JsonProperty("type")
        private String type;

        @JsonProperty("compeCode")
        private String compeCode;

        @JsonProperty("ispb")
        private String ispb;

        @JsonProperty("number")
        private String number;

        @JsonProperty("checkDigit")
        private String checkDigit;

        @JsonProperty("accountId")
        private String accountId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccountBalance {
        @JsonProperty("availableAmount")
        private AmountDetail availableAmount;

        @JsonProperty("blockedAmount")
        private AmountDetail blockedAmount;

        @JsonProperty("automaticallyInvestedAmount")
        private AmountDetail automaticallyInvestedAmount;

        @JsonProperty("overdraftContractedLimit")
        private AmountDetail overdraftContractedLimit;

        @JsonProperty("overdraftUsedLimit")
        private AmountDetail overdraftUsedLimit;

        @JsonProperty("unarrangedOverdraftAmount")
        private AmountDetail unarrangedOverdraftAmount;

        @JsonProperty("updateDateTime")
        private String updateDateTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccountTransaction {
        @JsonProperty("transactionId")
        private String transactionId;

        @JsonProperty("completedAuthorisedPaymentType")
        private String completedAuthorisedPaymentType;

        /** DEBITO ou CREDITO. */
        @JsonProperty("creditDebitIndicator")
        private String creditDebitIndicator;

        @JsonProperty("transactionName")
        private String transactionName;

        @JsonProperty("type")
        private String type;

        @JsonProperty("amount")
        private AmountDetail amount;

        @JsonProperty("transactionDate")
        private LocalDate transactionDate;

        @JsonProperty("partieCnpjCpf")
        private String partieCnpjCpf;

        @JsonProperty("partiePersonType")
        private String partiePersonType;

        @JsonProperty("partieCompeCode")
        private String partieCompeCode;

        @JsonProperty("partieIspb")
        private String partieIspb;

        @JsonProperty("partieNumber")
        private String partieNumber;

        @JsonProperty("partieCheckDigit")
        private String partieCheckDigit;

        /** Converte para BigDecimal com sinal: DEBITO = negativo, CREDITO = positivo. */
        public BigDecimal getSignedAmount() {
            BigDecimal value = amount != null ? amount.toBigDecimal() : BigDecimal.ZERO;
            return "DEBITO".equalsIgnoreCase(creditDebitIndicator) ? value.negate() : value;
        }
    }

    // ── Credit Cards ──────────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreditCard {
        @JsonProperty("creditCardAccountId")
        private String creditCardAccountId;

        @JsonProperty("brandName")
        private String brandName;

        @JsonProperty("companyCnpj")
        private String companyCnpj;

        @JsonProperty("name")
        private String name;

        @JsonProperty("productType")
        private String productType;

        @JsonProperty("creditCardNetwork")
        private String creditCardNetwork;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreditCardLimit {
        @JsonProperty("creditLineLimitType")
        private String creditLineLimitType;

        @JsonProperty("consolidationType")
        private String consolidationType;

        @JsonProperty("identificationNumber")
        private String identificationNumber;

        @JsonProperty("isLimitFlexible")
        private Boolean isLimitFlexible;

        @JsonProperty("limitAmountCurrency")
        private String limitAmountCurrency;

        @JsonProperty("limitAmount")
        private BigDecimal limitAmount;

        @JsonProperty("usedAmountCurrency")
        private String usedAmountCurrency;

        @JsonProperty("usedAmount")
        private BigDecimal usedAmount;

        @JsonProperty("availableAmountCurrency")
        private String availableAmountCurrency;

        @JsonProperty("availableAmount")
        private BigDecimal availableAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreditCardBill {
        @JsonProperty("billId")
        private String billId;

        @JsonProperty("dueDate")
        private LocalDate dueDate;

        @JsonProperty("totalAmount")
        private AmountDetail totalAmount;

        @JsonProperty("minimumAmount")
        private AmountDetail minimumAmount;

        @JsonProperty("isInstalment")
        private Boolean isInstalment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreditCardTransaction {
        @JsonProperty("transactionId")
        private String transactionId;

        @JsonProperty("identificationNumber")
        private String identificationNumber;

        @JsonProperty("transactionName")
        private String transactionName;

        @JsonProperty("billId")
        private String billId;

        @JsonProperty("creditDebitType")
        private String creditDebitType;

        @JsonProperty("transactionType")
        private String transactionType;

        @JsonProperty("transactionalAdditionalInfo")
        private String transactionalAdditionalInfo;

        @JsonProperty("paymentType")
        private String paymentType;

        @JsonProperty("feeType")
        private String feeType;

        @JsonProperty("feeTypeAdditionalInfo")
        private String feeTypeAdditionalInfo;

        @JsonProperty("otherCreditsType")
        private String otherCreditsType;

        @JsonProperty("otherCreditsAdditionalInfo")
        private String otherCreditsAdditionalInfo;

        @JsonProperty("amount")
        private AmountDetail amount;

        @JsonProperty("transactionDate")
        private LocalDate transactionDate;

        @JsonProperty("billPostDate")
        private LocalDate billPostDate;

        @JsonProperty("payeeMCC")
        private Integer payeeMCC;

        public BigDecimal getSignedAmount() {
            BigDecimal value = amount != null ? amount.toBigDecimal() : BigDecimal.ZERO;
            return "DEBITO".equalsIgnoreCase(creditDebitType) ? value.negate() : value;
        }
    }

    // ── Loans ─────────────────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Loan {
        @JsonProperty("contractId")
        private String contractId;

        @JsonProperty("brandName")
        private String brandName;

        @JsonProperty("companyCnpj")
        private String companyCnpj;

        @JsonProperty("productType")
        private String productType;

        @JsonProperty("productSubType")
        private String productSubType;

        @JsonProperty("ipocCode")
        private String ipocCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoanDetail {
        @JsonProperty("contractId")
        private String contractId;

        @JsonProperty("contractNumber")
        private String contractNumber;

        @JsonProperty("ipocCode")
        private String ipocCode;

        @JsonProperty("productName")
        private String productName;

        @JsonProperty("productType")
        private String productType;

        @JsonProperty("productSubType")
        private String productSubType;

        @JsonProperty("contractDate")
        private LocalDate contractDate;

        @JsonProperty("disbursementDates")
        private List<LocalDate> disbursementDates;

        @JsonProperty("settlementDate")
        private LocalDate settlementDate;

        @JsonProperty("contractAmount")
        private AmountDetail contractAmount;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("dueDate")
        private LocalDate dueDate;

        @JsonProperty("instalmentPeriodicity")
        private String instalmentPeriodicity;

        @JsonProperty("firstInstalmentDueDate")
        private LocalDate firstInstalmentDueDate;

        @JsonProperty("CET")
        private BigDecimal cet;

        @JsonProperty("amortizationScheduled")
        private String amortizationScheduled;

        @JsonProperty("cnpjConsignee")
        private String cnpjConsignee;

        @JsonProperty("interestRates")
        private List<InterestRate> interestRates;

        @JsonProperty("contractedFees")
        private List<ContractedFee> contractedFees;

        @JsonProperty("contractedFinanceCharges")
        private List<ContractedFinanceCharge> contractedFinanceCharges;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InterestRate {
        @JsonProperty("taxType")
        private String taxType;

        @JsonProperty("interestRateType")
        private String interestRateType;

        @JsonProperty("taxPeriodicity")
        private String taxPeriodicity;

        @JsonProperty("calculation")
        private String calculation;

        @JsonProperty("referentialRateIndexerType")
        private String referentialRateIndexerType;

        @JsonProperty("referentialRateIndexerSubType")
        private String referentialRateIndexerSubType;

        @JsonProperty("preFixedRate")
        private BigDecimal preFixedRate;

        @JsonProperty("postFixedRate")
        private BigDecimal postFixedRate;

        @JsonProperty("additionalInfo")
        private String additionalInfo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContractedFee {
        @JsonProperty("feeName")
        private String feeName;

        @JsonProperty("feeCode")
        private String feeCode;

        @JsonProperty("feeChargeType")
        private String feeChargeType;

        @JsonProperty("feeCharge")
        private String feeCharge;

        @JsonProperty("feeAmount")
        private BigDecimal feeAmount;

        @JsonProperty("feeRate")
        private BigDecimal feeRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContractedFinanceCharge {
        @JsonProperty("chargeType")
        private String chargeType;

        @JsonProperty("chargeAdditionalInfo")
        private String chargeAdditionalInfo;

        @JsonProperty("chargeRate")
        private BigDecimal chargeRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoanWarranty {
        @JsonProperty("currency")
        private String currency;

        @JsonProperty("warrantyType")
        private String warrantyType;

        @JsonProperty("warrantySubType")
        private String warrantySubType;

        @JsonProperty("warrantyAmount")
        private AmountDetail warrantyAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoanPayments {
        @JsonProperty("paidInstalments")
        private Integer paidInstalments;

        @JsonProperty("contractOutstandingBalance")
        private AmountDetail contractOutstandingBalance;

        @JsonProperty("releases")
        private List<PaymentRelease> releases;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentRelease {
        @JsonProperty("paymentId")
        private String paymentId;

        @JsonProperty("isOverParcelPayment")
        private Boolean isOverParcelPayment;

        @JsonProperty("instalmentId")
        private String instalmentId;

        @JsonProperty("paidDate")
        private LocalDate paidDate;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("paidAmount")
        private AmountDetail paidAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoanScheduledInstalments {
        @JsonProperty("typeNumberOfInstalments")
        private String typeNumberOfInstalments;

        @JsonProperty("totalNumberOfInstalments")
        private Integer totalNumberOfInstalments;

        @JsonProperty("typeContractRemaining")
        private String typeContractRemaining;

        @JsonProperty("contractRemainingNumber")
        private Integer contractRemainingNumber;

        @JsonProperty("paidInstalments")
        private Integer paidInstalments;

        @JsonProperty("dueInstalments")
        private Integer dueInstalments;

        @JsonProperty("pastDueInstalments")
        private Integer pastDueInstalments;

        @JsonProperty("balloonInstalments")
        private List<BalloonInstalment> balloonInstalments;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BalloonInstalment {
        @JsonProperty("dueDate")
        private LocalDate dueDate;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("amount")
        private AmountDetail amount;
    }

    // ── Financings ────────────────────────────────────────────────────────────
    // Estrutura idêntica a Loans — reutiliza os mesmos tipos de detalhe.

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Financing {
        @JsonProperty("contractId")
        private String contractId;

        @JsonProperty("brandName")
        private String brandName;

        @JsonProperty("companyCnpj")
        private String companyCnpj;

        @JsonProperty("productType")
        private String productType;

        @JsonProperty("productSubType")
        private String productSubType;

        @JsonProperty("ipocCode")
        private String ipocCode;
    }

    // ── Invoice Financings ────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InvoiceFinancing {
        @JsonProperty("contractId")
        private String contractId;

        @JsonProperty("brandName")
        private String brandName;

        @JsonProperty("companyCnpj")
        private String companyCnpj;

        @JsonProperty("productType")
        private String productType;

        @JsonProperty("productSubType")
        private String productSubType;

        @JsonProperty("ipocCode")
        private String ipocCode;
    }

    // ── Customers ─────────────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CustomerPersonal {
        @JsonProperty("updateDateTime")
        private String updateDateTime;

        @JsonProperty("personalId")
        private PersonalId personalId;

        @JsonProperty("contacts")
        private PersonalContacts contacts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PersonalId {
        @JsonProperty("brandName")
        private String brandName;

        @JsonProperty("civilName")
        private String civilName;

        @JsonProperty("socialName")
        private String socialName;

        @JsonProperty("birthDate")
        private LocalDate birthDate;

        @JsonProperty("maritalStatusCode")
        private String maritalStatusCode;

        @JsonProperty("sex")
        private String sex;

        @JsonProperty("cpfNumber")
        private String cpfNumber;

        @JsonProperty("documents")
        private List<PersonalDocument> documents;

        @JsonProperty("nationality")
        private List<Nationality> nationality;

        @JsonProperty("filiation")
        private List<Filiation> filiation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PersonalDocument {
        @JsonProperty("type")
        private String type;

        @JsonProperty("number")
        private String number;

        @JsonProperty("expirationDate")
        private LocalDate expirationDate;

        @JsonProperty("issueDate")
        private LocalDate issueDate;

        @JsonProperty("country")
        private String country;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nationality {
        @JsonProperty("otherNationalitiesInfo")
        private String otherNationalitiesInfo;

        @JsonProperty("documents")
        private List<NationalityDocument> documents;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NationalityDocument {
        @JsonProperty("documentType")
        private String documentType;

        @JsonProperty("number")
        private String number;

        @JsonProperty("expirationDate")
        private LocalDate expirationDate;

        @JsonProperty("issueDate")
        private LocalDate issueDate;

        @JsonProperty("country")
        private String country;

        @JsonProperty("typeAdditionalInfo")
        private String typeAdditionalInfo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Filiation {
        @JsonProperty("type")
        private String type;

        @JsonProperty("civilName")
        private String civilName;

        @JsonProperty("socialName")
        private String socialName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PersonalContacts {
        @JsonProperty("postalAddresses")
        private List<PostalAddress> postalAddresses;

        @JsonProperty("phones")
        private List<Phone> phones;

        @JsonProperty("emails")
        private List<Email> emails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostalAddress {
        @JsonProperty("isMain")
        private Boolean isMain;

        @JsonProperty("address")
        private String address;

        @JsonProperty("additionalInfo")
        private String additionalInfo;

        @JsonProperty("districtName")
        private String districtName;

        @JsonProperty("townName")
        private String townName;

        @JsonProperty("ibgeTownCode")
        private String ibgeTownCode;

        @JsonProperty("countrySubDivision")
        private String countrySubDivision;

        @JsonProperty("postCode")
        private String postCode;

        @JsonProperty("country")
        private String country;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Phone {
        @JsonProperty("isMain")
        private Boolean isMain;

        @JsonProperty("type")
        private String type;

        @JsonProperty("countryCallingCode")
        private String countryCallingCode;

        @JsonProperty("areaCode")
        private String areaCode;

        @JsonProperty("number")
        private String number;

        @JsonProperty("phoneExtension")
        private String phoneExtension;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Email {
        @JsonProperty("isMain")
        private Boolean isMain;

        @JsonProperty("email")
        private String email;
    }

    // ── Investments ───────────────────────────────────────────────────────────

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InvestmentFund {
        @JsonProperty("investmentId")
        private String investmentId;

        @JsonProperty("brandName")
        private String brandName;

        @JsonProperty("companyCnpj")
        private String companyCnpj;

        @JsonProperty("anbimaCategory")
        private String anbimaCategory;

        @JsonProperty("anbimaClass")
        private String anbimaClass;

        @JsonProperty("anbimaSubClass")
        private String anbimaSubClass;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InvestmentFundBalance {
        @JsonProperty("financialSystemAccountNumber")
        private String financialSystemAccountNumber;

        @JsonProperty("updateDateTime")
        private String updateDateTime;

        @JsonProperty("quotaQuantity")
        private String quotaQuantity;

        @JsonProperty("blockedBalance")
        private AmountDetail blockedBalance;

        @JsonProperty("quotaGrossPriceValue")
        private AmountDetail quotaGrossPriceValue;

        @JsonProperty("grossAmount")
        private AmountDetail grossAmount;

        @JsonProperty("netAmount")
        private AmountDetail netAmount;

        @JsonProperty("incomeTaxProvision")
        private AmountDetail incomeTaxProvision;

        @JsonProperty("financialTransactionTaxProvision")
        private AmountDetail financialTransactionTaxProvision;

        @JsonProperty("purchaseUnitPrice")
        private AmountDetail purchaseUnitPrice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BankFixedIncome {
        @JsonProperty("investmentId")
        private String investmentId;

        @JsonProperty("brandName")
        private String brandName;

        @JsonProperty("companyCnpj")
        private String companyCnpj;

        @JsonProperty("investmentType")
        private String investmentType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BankFixedIncomeBalance {
        @JsonProperty("updateDateTime")
        private String updateDateTime;

        @JsonProperty("preFixedRate")
        private BigDecimal preFixedRate;

        @JsonProperty("postFixedIndexerPercentage")
        private BigDecimal postFixedIndexerPercentage;

        @JsonProperty("financialTransactionTaxProvision")
        private AmountDetail financialTransactionTaxProvision;

        @JsonProperty("incomeTaxProvision")
        private AmountDetail incomeTaxProvision;

        @JsonProperty("paidIncomeValue")
        private AmountDetail paidIncomeValue;

        @JsonProperty("grossAmount")
        private AmountDetail grossAmount;

        @JsonProperty("netAmount")
        private AmountDetail netAmount;

        @JsonProperty("incomeFactor")
        private BigDecimal incomeFactor;

        @JsonProperty("purchaseUnitPrice")
        private AmountDetail purchaseUnitPrice;

        @JsonProperty("grossAmountAtPurchase")
        private AmountDetail grossAmountAtPurchase;
    }
}