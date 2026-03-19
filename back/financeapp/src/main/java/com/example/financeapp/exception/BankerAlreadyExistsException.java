package com.example.financeapp.exception;

public class BankerAlreadyExistsException extends BusinessException {
    public BankerAlreadyExistsException(String email) {
        super(ErrorCode.BANKER_ALREADY_EXISTS, "Personal banker já cadastrado com o e-mail: " + email);
    }
}
