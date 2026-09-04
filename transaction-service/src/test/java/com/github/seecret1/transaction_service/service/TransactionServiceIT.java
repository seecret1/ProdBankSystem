package com.github.seecret1.transaction_service.service;

import com.github.seecret1.transaction_service.AbstractIntegrationTest;
import com.github.seecret1.transaction_service.dto.message.TransactionMessage;
import com.github.seecret1.transaction_service.dto.payment.PaymentResponse;
import com.github.seecret1.transaction_service.dto.transaction.TransactionDto;
import com.github.seecret1.transaction_service.entity.Transaction;
import com.github.seecret1.transaction_service.entity.enums.PaymentStatus;
import com.github.seecret1.transaction_service.entity.enums.PaymentType;
import com.github.seecret1.transaction_service.kafka.producer.TransactionMessageKafkaProducerService;
import com.github.seecret1.transaction_service.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@DisplayName("TransactionService Integration Tests (H2 + Mock Kafka)")
class TransactionServiceIT extends AbstractIntegrationTest {

    @Autowired private TransactionService transactionService;
    @Autowired private TransactionRepository transactionRepository;
    @MockitoBean private TransactionMessageKafkaProducerService producerService;

    @Test @DisplayName("processRequest: should persist transaction and set PROCESSING")
    void shouldProcessRequest() {
        PaymentResponse pay = new PaymentResponse("pay-it-1", "src-it-1", "dst-it-1", new BigDecimal("200.00"), PaymentType.CARD_PAYMENT, "RUB");
        TransactionMessage msg = TransactionMessage.builder().traceId(UUID.randomUUID().toString()).userId("user-it-1").sourceInvoiceId("src-it-1").destinationInvoiceId("dst-it-1").amount(new BigDecimal("200.00")).currency("RUB").paymentType(PaymentType.CARD_PAYMENT).status(PaymentStatus.CREATED).data(pay).build();

        transactionService.processRequest(msg);

        assertThat(transactionRepository.count()).isGreaterThan(0);
        Transaction saved = transactionRepository.findAll().stream().filter(t -> t.getPaymentId().equals("pay-it-1")).findFirst().orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(com.github.seecret1.transaction_service.entity.enums.TransactionStatus.PROCESSING);
        assertThat(msg.getData()).isInstanceOf(TransactionDto.class);
        verify(producerService).sendWithWait(any(), any(TransactionMessage.class));
    }

    @Test @DisplayName("processResponse: should update status to COMPLETED")
    void shouldProcessResponse() {
        // first create
        PaymentResponse pay = new PaymentResponse("pay-resp-1", "src-1", "dst-1", new BigDecimal("50.00"), PaymentType.TRANSFER, "USD");
        TransactionMessage req = TransactionMessage.builder().traceId(UUID.randomUUID().toString()).userId("u1").sourceInvoiceId("src-1").destinationInvoiceId("dst-1").amount(new BigDecimal("50.00")).currency("USD").paymentType(PaymentType.TRANSFER).status(PaymentStatus.CREATED).data(pay).build();
        transactionService.processRequest(req);
        Transaction created = transactionRepository.findAll().stream().filter(t -> t.getPaymentId().equals("pay-resp-1")).findFirst().orElseThrow();
        TransactionDto dto = new TransactionDto(created.getId(), "u1", "pay-resp-1", "src-1", "dst-1", new BigDecimal("50.00"), "USD", created.getTransactionType(), created.getStatus());

        TransactionMessage resp = TransactionMessage.builder().traceId(req.getTraceId()).userId("u1").sourceInvoiceId("src-1").destinationInvoiceId("dst-1").amount(new BigDecimal("50.00")).currency("USD").paymentType(PaymentType.TRANSFER).status(PaymentStatus.COMPLETED).data(dto).build();
        transactionService.processResponse(resp);

        Transaction updated = transactionRepository.findById(created.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(com.github.seecret1.transaction_service.entity.enums.TransactionStatus.COMPLETED);
        verify(producerService, org.mockito.Mockito.atLeastOnce()).sendWithWait(any(), any());
    }
}
