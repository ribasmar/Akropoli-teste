package com.example.financeapp;

import com.example.financeapp.client.dto.ClientDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinanceappApplicationTests {

	@Test
	void maskCpfShouldExposeOnlyMiddleDigits() {
		assertEquals("***456.789-**", ClientDto.maskCpf("12345678901"));
	}
}