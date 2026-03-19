package com.example.financeapp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração de CORS para a aplicação.
 *
 * A lista de origens permitidas é lida do application.yml via:
 *   cors.allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173}
 *
 * Em produção, configure a variável de ambiente CORS_ALLOWED_ORIGINS
 * com o domínio real do frontend (ex: https://app.seudominio.com).
 *
 * O SecurityConfig já chama .cors(Customizer.withDefaults()), que delega
 * para este CorsFilter registrado no contexto Spring.
 */
@Slf4j
@Configuration
public class CorsConfig {

    /**
     * Lista de origens permitidas, separadas por vírgula no YAML/env.
     * Spring converte automaticamente "a,b,c" → List<String>.
     */
    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        log.info("CORS allowed origins: {}", allowedOrigins);

        CorsConfiguration config = new CorsConfiguration();

        // Origens vindas do application.yml / variável de ambiente
        config.setAllowedOrigins(allowedOrigins);

        // Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Headers que o cliente pode enviar (incluindo Authorization para JWT)
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control"
        ));

        // Headers que o browser pode acessar na resposta
        config.setExposedHeaders(List.of("Authorization"));

        // Permite envio de cookies/credentials (necessário para refresh token em cookie)
        config.setAllowCredentials(true);

        // Cache do preflight por 1 hora (reduz OPTIONS desnecessários)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}