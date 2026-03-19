package com.example.financeapp.exception;

/**
 * Exceção lançada quando a integração com a API Akropoli falha.
 *
 * Usa factory methods estáticos para garantir mensagens consistentes
 * e facilitar o tratamento no GlobalExceptionHandler.
 */
public class AkropoliIntegrationException extends RuntimeException {

    private final String errorCode;
    private final String apiResponse;

    private AkropoliIntegrationException(String message, String errorCode, String apiResponse) {
        super(message);
        this.errorCode = errorCode;
        this.apiResponse = apiResponse;
    }

    private AkropoliIntegrationException(String message, String errorCode, String apiResponse, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.apiResponse = apiResponse;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /** Retorna detalhes da resposta de erro da API externa (Akropoli). */
    public String getApiResponse() {
        return apiResponse;
    }

    public static AkropoliIntegrationException authenticationFailed(Throwable cause) {
        return new AkropoliIntegrationException(
                "Failed to authenticate with Akropoli API",
                "AKROPOLI_AUTH_FAILED",
                cause.getMessage(),
                cause);
    }

    public static AkropoliIntegrationException fetchFailed(String endpoint, Throwable cause) {
        return new AkropoliIntegrationException(
                "Failed to fetch data from Akropoli endpoint: " + endpoint,
                "AKROPOLI_FETCH_FAILED",
                cause.getMessage(),
                cause);
    }

    public static AkropoliIntegrationException linkIdNotFound(String clientId) {
        return new AkropoliIntegrationException(
                "Client has no active Akropoli linkId: " + clientId,
                "AKROPOLI_LINK_ID_NOT_FOUND",
                null);
    }

    public static AkropoliIntegrationException consentExpired(String clientId) {
        return new AkropoliIntegrationException(
                "Akropoli consent has expired for client: " + clientId,
                "AKROPOLI_CONSENT_EXPIRED",
                null);
    }

    public static AkropoliIntegrationException consentScopeMissing(String endpoint) {
        return new AkropoliIntegrationException(
                "Resource not authorized in consent scope: " + endpoint,
                "AKROPOLI_CONSENT_SCOPE_MISSING",
                null);
    }
}
