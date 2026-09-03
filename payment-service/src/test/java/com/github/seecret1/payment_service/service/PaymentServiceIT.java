package com.github.seecret1.payment_service.service;

import com.github.seecret1.payment_service.AbstractIntegrationTest;
import com.github.seecret1.payment_service.dto.payment.PaymentRequest;
import com.github.seecret1.payment_service.dto.payment.PaymentResponse;
import com.github.seecret1.payment_service.entity.enums.PaymentType;
import com.github.seecret1.payment_service.kafka.producer.PaymentMessageKafkaProducerService;
import com.github.seecret1.payment_service.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@DisplayName("PaymentService Integration Tests (H2 + Mock Kafka)")
class PaymentServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PaymentMessageKafkaProducerService producerService;

    @Test
    @DisplayName("create: should persist payment and call producer")
    void shouldCreateAndPersist() {
        PaymentRequest request = new PaymentRequest("src-it-1", "dst-it-1", new BigDecimal("250.00"), PaymentType.CARD_PAYMENT, "RUB");
        String userId = "user-it-1";

        PaymentResponse response = paymentService.create(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.sourceInvoiceId()).isEqualTo("src-it-1");
        assertThat(response.amount()).isEqualByComparingTo("250.00");
        assertThat(response.id()).isNotNull();

        assertThat(paymentRepository.findById(response.id())).isPresent();
        verify(producerService).sendWithWait(any());
        assertThat(paymentRepository.findById(response.id()).get().getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("create: multiple payments should have distinct ids")
    void shouldCreateMultiplePayments() {
        var r1 = paymentService.create("u1", new PaymentRequest("s1", "d1", new BigDecimal("10.00"), PaymentType.TRANSFER, "RUB"));
        var r2 = paymentService.create("u2", new PaymentRequest("s2", "d2", new BigDecimal("20.00"), PaymentType.TRANSFER, "USD"));
        assertThat(r1.id()).isNotEqualTo(r2.id());
        assertThat(paymentRepository.count()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("create: should handle all PaymentTypes")
    void shouldHandleAllTypes() {
        for (PaymentType type : PaymentType.values()) {
            PaymentRequest req = new PaymentRequest("src", "dst", new BigDecimal("5.00"), type, "RUB");
            PaymentResponse res = paymentService.create("user-all-types", req);
            assertThat(res.type()).isEqualTo(type);
        }
    }
}
