package com.example.financeapp.auth.service;

import com.example.financeapp.auth.dto.AuthDto;
import com.example.financeapp.auth.model.Banker;
import com.example.financeapp.auth.model.RefreshToken;
import com.example.financeapp.auth.repository.BankerRepository;
import com.example.financeapp.auth.repository.RefreshTokenRepository;
import com.example.financeapp.config.encryption.DeterministicHashService;
import com.example.financeapp.exception.BankerAlreadyExistsException;
import com.example.financeapp.exception.BankerNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final BankerRepository repository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final DeterministicHashService hashService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    // ── Registro ──────────────────────────────────────────────────────────────

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        log.info("Registering banker: {}", request.getEmail());

        // existsByEmail → existsByEmailHash (email criptografado no MongoDB)
        String emailHash = hashService.hashEmail(request.getEmail());
        if (repository.existsByEmailHash(emailHash)) {
            throw new BankerAlreadyExistsException(request.getEmail());
        }

        Banker banker = Banker.builder()
                .name(request.getName())
                .email(request.getEmail())   // criptografado pelo EncryptionMongoEventListener
                .emailHash(emailHash)        // hash determinístico para buscas
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        Banker saved = repository.save(banker);

        String accessToken  = jwtService.generateToken(saved);
        String refreshToken = jwtService.generateRefreshToken(saved);
        persistRefreshToken(refreshToken, saved.getId());

        return toResponse(saved, accessToken, refreshToken);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Transactional
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        log.info("Login attempt: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        // findByEmail → findByEmailHash (email criptografado no MongoDB)
        String emailHash = hashService.hashEmail(request.getEmail());
        Banker banker = repository.findByEmailHash(emailHash)
                .orElseThrow(() -> new BankerNotFoundException(request.getEmail()));

        String accessToken  = jwtService.generateToken(banker);
        String refreshToken = jwtService.generateRefreshToken(banker);
        persistRefreshToken(refreshToken, banker.getId());

        return toResponse(banker, accessToken, refreshToken);
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Transactional
    public AuthDto.AuthResponse refresh(AuthDto.RefreshRequest request) {
        String incomingToken = request.getRefreshToken();

        final String email;
        try {
            email = jwtService.extractEmail(incomingToken);
        } catch (Exception e) {
            log.warn("Refresh token com assinatura inválida");
            throw new IllegalArgumentException("Refresh token inválido");
        }

        String tokenHash = sha256(incomingToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Refresh token não encontrado no banco para: {}", email);
                    return new IllegalArgumentException("Refresh token não reconhecido");
                });

        if (!stored.isValid()) {
            log.warn("Refresh token inválido (revogado={}, expirado={}) para: {}",
                    stored.isRevoked(), stored.isExpired(), email);
            if (stored.isRevoked()) {
                log.error("Possível reutilização de token revogado para banker: {}. Revogando todos.", email);
                refreshTokenRepository.revokeAllByBankerId(stored.getBankerId(), LocalDateTime.now());
            }
            throw new IllegalArgumentException("Refresh token inválido ou expirado");
        }

        // findByEmail → findByEmailHash
        String emailHash = hashService.hashEmail(email);
        Banker banker = repository.findByEmailHash(emailHash)
                .orElseThrow(() -> new BankerNotFoundException(email));

        stored.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(stored);

        String newAccessToken  = jwtService.generateToken(banker);
        String newRefreshToken = jwtService.generateRefreshToken(banker);
        persistRefreshToken(newRefreshToken, banker.getId());

        log.info("Tokens renovados para banker: {}", email);
        return toResponse(banker, newAccessToken, newRefreshToken);
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Transactional
    public void logout(String bankerId) {
        int revoked = refreshTokenRepository.revokeAllByBankerId(bankerId, LocalDateTime.now());
        log.info("Logout: {} refresh token(s) revogado(s) para banker: {}", revoked, bankerId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void persistRefreshToken(String rawToken, String bankerId) {
        LocalDateTime expiresAt = LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000);
        refreshTokenRepository.save(RefreshToken.builder()
                .tokenHash(sha256(rawToken))
                .bankerId(bankerId)
                .expiresAt(expiresAt)
                .build());
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível", e);
        }
    }

    private AuthDto.AuthResponse toResponse(Banker banker, String token, String refreshToken) {
        return AuthDto.AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .id(banker.getId())
                .name(banker.getName())
                .email(banker.getEmail())
                .role(banker.getRole().name())
                .build();
    }
}