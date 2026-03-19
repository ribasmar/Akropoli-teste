package com.example.financeapp.exception;

public class BankerNotFoundException extends BusinessException {
    public BankerNotFoundException(String identifier) {
        super(ErrorCode.BANKER_NOT_FOUND, "Personal banker não encontrado: " + identifier);
    }
}
