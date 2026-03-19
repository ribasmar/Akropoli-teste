package com.example.financeapp.auth.controller;

import com.example.financeapp.auth.dto.AuthDto;
import com.example.financeapp.auth.model.Banker;
import com.example.financeapp.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping(
            value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthDto.AuthResponse> register(
            @RequestBody @Valid AuthDto.RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthDto.AuthResponse> login(
            @RequestBody @Valid AuthDto.LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    /**
     * CORREÇÃO F-01: renova o par de tokens usando um refresh token válido.
     *
     * - Endpoint público (sem JWT no header) — o refresh token é a credencial
     * - Implementa rotação automática: token atual é revogado, novo é emitido
     * - Em caso de token inválido, expirado ou já revogado: 401
     */
    @PostMapping(
            value = "/refresh",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthDto.AuthResponse> refresh(
            @RequestBody @Valid AuthDto.RefreshRequest request) {
        try {
            return ResponseEntity.ok(service.refresh(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * CORREÇÃO F-01: revoga todos os refresh tokens do banker autenticado.
     *
     * - Requer Bearer token válido no header (endpoint protegido)
     * - O access token atual expira naturalmente pelo TTL configurado
     * - Após logout, qualquer /refresh com tokens antigos retorna 401
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Banker banker) {
        service.logout(banker.getId());
        return ResponseEntity.noContent().build();
    }
}