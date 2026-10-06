package br.com.fiap.delivery.paymentservice.dto;

import java.math.BigDecimal;

public record PaymentRequest(BigDecimal amount) {
}
