package com.example.financeapp.exception;

public class ClientNotFoundException extends BusinessException {
    public ClientNotFoundException(String identifier) {
        super(ErrorCode.CLIENT_NOT_FOUND, "Cliente não encontrado: " + identifier);
    }
}
