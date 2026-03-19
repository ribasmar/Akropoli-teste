package com.example.financeapp.exception;

/**
 * Exceção lançada quando a integração com a API Akropoli falha.
 *
 * Usa factory methods estáticos para garantir mensagens consistentes
 * e facilitar o tratamento no GlobalExceptionHandler.
 */
public class AkropoliIntegrationException extends RuntimeException {

    private final String errorCode;
    private final String pluggyResponse; // mantido como "pluggyResponse" para compatibilidade com GlobalExceptionHandler

    private AkropoliIntegrationException(String message, String errorCode, String pluggyResponse) {
        super(message);
        this.errorCode     = errorCode;
        this.pluggyResponse = pluggyResponse;
    }

    private AkropoliIntegrationException(String message, String errorCode, String pluggyResponse, Throwable cause) {
        super(message, cause);
        this.errorCode     = errorCode;
        this.pluggyResponse = pluggyResponse;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getErrorCode() {
        return errorCode;
    }

    /** Retorna detalhes da resposta de erro da API externa (Akropoli). */
    public String getPluggyResponse() {
        return pluggyResponse;
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    /**
     * Falha de autenticação ao obter token de acesso Akropoli.
     */
    public static AkropoliIntegrationException authenticationFailed(Throwable cause) {
        return new AkropoliIntegrationException(
                "Failed to authenticate with Akropoli API",
                "AKROPOLI_AUTH_FAILED",
                cause.getMessage(),
                cause);
    }

    /**
     * Falha ao buscar dados de um endpoint específico.
     *
     * @param endpoint  path da URL que falhou
     * @param cause     exceção original (WebClientResponseException)
     */
    public static AkropoliIntegrationException fetchFailed(String endpoint, Throwable cause) {
        return new AkropoliIntegrationException(
                "Failed to fetch data from Akropoli endpoint: " + endpoint,
                "AKROPOLI_FETCH_FAILED",
                cause.getMessage(),
                cause);
    }

    /**
     * Cliente não possui linkId associado — consentimento ainda não foi concedido.
     *
     * @param clientId  ID do cliente no MongoDB
     */
    public static AkropoliIntegrationException linkIdNotFound(String clientId) {
        return new AkropoliIntegrationException(
                "Client has no active Akropoli linkId: " + clientId,
                "AKROPOLI_LINK_ID_NOT_FOUND",
                null);
    }

    /**
     * Consentimento do cliente expirou — renovação necessária.
     *
     * @param clientId  ID do cliente no MongoDB
     */
    public static AkropoliIntegrationException consentExpired(String clientId) {
        return new AkropoliIntegrationException(
                "Akropoli consent has expired for client: " + clientId,
                "AKROPOLI_CONSENT_EXPIRED",
                null);
    }

    /**
     * Endpoint chamado fora do escopo autorizado pelo usuário (HTTP 403).
     *
     * @param endpoint  path da URL que retornou 403
     */
    public static AkropoliIntegrationException consentScopeMissing(String endpoint) {
        return new AkropoliIntegrationException(
                "Resource not authorized in consent scope: " + endpoint,
                "AKROPOLI_CONSENT_SCOPE_MISSING",
                null);
    }
}