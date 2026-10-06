package br.com.fiap.delivery.paymentservice.service;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.fiap.delivery.paymentservice.dto.PaymentRequest;
import br.com.fiap.delivery.paymentservice.dto.PaymentResponse;
import br.com.fiap.delivery.paymentservice.exception.PaymentFailedException;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final Random random;
    private final int instancePort;

    public PaymentService(Random random, @Value("${server.port}") int instancePort) {
        this.random = random;
        this.instancePort = instancePort;
    }

    public PaymentResponse process(PaymentRequest request) {
        if (random.nextBoolean()) {
            log.info("Payment APPROVED by instance {}, amount={}", instancePort, request.amount());
            return new PaymentResponse("APPROVED", instancePort);
        }
        log.warn("Payment FAILED on instance {}, amount={}", instancePort, request.amount());
        throw new PaymentFailedException("Payment failed");
    }

}
