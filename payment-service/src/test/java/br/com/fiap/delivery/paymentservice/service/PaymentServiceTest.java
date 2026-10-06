package br.com.fiap.delivery.paymentservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Random;

import org.junit.jupiter.api.Test;

import br.com.fiap.delivery.paymentservice.dto.PaymentRequest;
import br.com.fiap.delivery.paymentservice.dto.PaymentResponse;
import br.com.fiap.delivery.paymentservice.exception.PaymentFailedException;

class PaymentServiceTest {

    private static final PaymentRequest REQUEST = new PaymentRequest(new BigDecimal("79.80"));

    private static Random fixedRandom(boolean value) {
        return new Random() {
            @Override
            public boolean nextBoolean() {
                return value;
            }
        };
    }

    @Test
    void approvesPaymentWithInstancePort() {
        PaymentService service = new PaymentService(fixedRandom(true), 8082);

        PaymentResponse response = service.process(REQUEST);

        assertEquals("APPROVED", response.status());
        assertEquals(8082, response.instance());
    }

    @Test
    void failsPaymentWhenRandomSaysSo() {
        PaymentService service = new PaymentService(fixedRandom(false), 8081);

        PaymentFailedException ex = assertThrows(PaymentFailedException.class, () -> service.process(REQUEST));

        assertEquals("Payment failed", ex.getMessage());
    }

}
