package com.example.financeapp.exception;

public class ClientAlreadyExistsException extends BusinessException {
    public ClientAlreadyExistsException(String cpf) {
        super(ErrorCode.CLIENT_ALREADY_EXISTS, "Cliente já cadastrado com o CPF: " + cpf);
    }
}
