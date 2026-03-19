package com.example.financeapp.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // ── Auth ──────────────────────────────────────────────────────────────────
    INVALID_CREDENTIALS("AUTH_001", "Credenciais inválidas", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("AUTH_002", "Token expirado", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("AUTH_003", "Token inválido", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("AUTH_004", "Acesso negado", HttpStatus.FORBIDDEN),
    BANKER_NOT_FOUND("AUTH_005", "Personal banker não encontrado", HttpStatus.NOT_FOUND),
    BANKER_ALREADY_EXISTS("AUTH_006", "Personal banker já cadastrado", HttpStatus.CONFLICT),

    // ── Client ────────────────────────────────────────────────────────────────
    CLIENT_NOT_FOUND("CLIENT_001", "Cliente não encontrado", HttpStatus.NOT_FOUND),
    CLIENT_ALREADY_EXISTS("CLIENT_002", "Cliente já cadastrado", HttpStatus.CONFLICT),
    CLIENT_INVALID_DATA("CLIENT_003", "Dados do cliente inválidos", HttpStatus.UNPROCESSABLE_ENTITY),

    // ── Pluggy ────────────────────────────────────────────────────────────────
    PLUGGY_AUTHENTICATION_FAILED("PLUGGY_001", "Falha na autenticação com a Pluggy", HttpStatus.BAD_GATEWAY),
    PLUGGY_CONNECT_TOKEN_FAILED("PLUGGY_002", "Falha ao gerar connect token", HttpStatus.BAD_GATEWAY),
    PLUGGY_ITEM_NOT_FOUND("PLUGGY_003", "Item Pluggy não encontrado", HttpStatus.NOT_FOUND),
    PLUGGY_ACCOUNTS_FETCH_FAILED("PLUGGY_004", "Falha ao buscar contas", HttpStatus.BAD_GATEWAY),
    PLUGGY_TRANSACTIONS_FETCH_FAILED("PLUGGY_005", "Falha ao buscar transações", HttpStatus.BAD_GATEWAY),
    PLUGGY_INVESTMENTS_FETCH_FAILED("PLUGGY_006", "Falha ao buscar investimentos", HttpStatus.BAD_GATEWAY),
    PLUGGY_TIMEOUT("PLUGGY_007", "Timeout na integração com a Pluggy", HttpStatus.GATEWAY_TIMEOUT),
    PLUGGY_TOKEN_NOT_FOUND("PLUGGY_008", "Token de conexão Pluggy não encontrado para este cliente", HttpStatus.NOT_FOUND),

    // ── Dashboard ─────────────────────────────────────────────────────────────
    DASHBOARD_DATA_UNAVAILABLE("DASH_001", "Dados de dashboard indisponíveis", HttpStatus.NOT_FOUND),
    DASHBOARD_PROCESSING_ERROR("DASH_002", "Erro ao processar dados do dashboard", HttpStatus.INTERNAL_SERVER_ERROR),

    // ── Genérico ──────────────────────────────────────────────────────────────
    VALIDATION_ERROR("GEN_001", "Erro de validação", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("GEN_002", "Erro interno do servidor", HttpStatus.INTERNAL_SERVER_ERROR),
    RESOURCE_NOT_FOUND("GEN_003", "Recurso não encontrado", HttpStatus.NOT_FOUND);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    public String getCode()           { return code; }
    public String getDefaultMessage() { return defaultMessage; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}
