package com.example.financeapp.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Document(collection = "bankers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Banker implements UserDetails {

    @Id
    private String id;

    private String name;

    // ── Campo criptografado (AES-256-GCM via EncryptionMongoEventListener) ───

    /** Email em ciphertext — nunca comparar diretamente em queries. */
    private String email;

    /**
     * HMAC-SHA256 do email (lowercase) — usado em findByEmailHash().
     * com criptografia por campo (ciphertexts distintos para o mesmo email).
     */
    @Indexed(unique = true)
    private String emailHash;

    // ── Campos normais ────────────────────────────────────────────────────────

    private String password;

    @Builder.Default
    private Role role = Role.BANKER;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }

    public enum Role {
        BANKER,
        ADMIN
    }
}