package com.example.financeapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifica que o contexto Spring sobe sem erros.
 *
 * @SpringBootTest sobe o ApplicationContext completo.
 * As propriedades são lidas de src/test/resources/application.yml,
 * que define valores fixos para jwt.secret, pluggy.*, cors.*, etc.
 *
 * Se este teste falhar com "Could not resolve placeholder":
 *   → confirme que src/test/resources/application.yml existe no projeto.
 *
 * Se falhar com erro de conexão ao banco:
 *   → os bancos (MongoDB + PostgreSQL) precisam estar rodando localmente,
 *     ou use @MockBean / Testcontainers para isolar os testes de infraestrutura.
 */
@SpringBootTest
class FinanceappApplicationTests {

	@Test
	void contextLoads() {
		// Apenas verifica que o contexto sobe sem exceção.
		// Se chegar aqui, todos os beans foram criados com sucesso.
	}
}