package com.example.financeapp.exception;

// ── Auth ─────────────────────────────────────────────────────────────────────

public class InvalidCredentialsException extends BusinessException {
    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }
}
