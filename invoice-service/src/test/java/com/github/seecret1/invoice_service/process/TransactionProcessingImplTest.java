package com.github.seecret1.invoice_service.process;

import com.github.seecret1.invoice_service.dto.message.TransactionMessage;
import com.github.seecret1.invoice_service.entity.enums.PaymentStatus;
import com.github.seecret1.invoice_service.entity.enums.PaymentType;
import com.github.seecret1.invoice_service.kafka.producer.TransactionMessageKafkaProducerService;
import com.github.seecret1.invoice_service.service.TransactionService;
import com.github.seecret1.invoice_service.service.process.TransactionProcessingImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionProcessingImpl Unit Tests")
class TransactionProcessingImplTest {

    @Mock private TransactionService transactionService;
    @Mock private TransactionMessageKafkaProducerService producerService;
    @InjectMocks private TransactionProcessingImpl processing;

    @Test @DisplayName("should call transactionService and then producer")
    void shouldProcessTransaction() {
        TransactionMessage msg = TransactionMessage.builder().traceId("t1").userId("u1").sourceInvoiceId("src1").destinationInvoiceId("dst1").amount(new BigDecimal("100.00")).currency("RUB").paymentType(PaymentType.TRANSFER).status(PaymentStatus.PROCESSING).build();
        TransactionMessage processed = TransactionMessage.builder().traceId("t1").status(PaymentStatus.PROCESSING).build();
        when(transactionService.transactionProcessing(msg)).thenReturn(processed);

        processing.transactionProcessing(msg);

        verify(transactionService).transactionProcessing(msg);
        verify(producerService).sendWithWait(processed);
    }

    @Test @DisplayName("should propagate exception from transactionService")
    void shouldPropagateException() {
        TransactionMessage msg = new TransactionMessage();
        when(transactionService.transactionProcessing(msg)).thenThrow(new RuntimeException("fail"));
        try {
            processing.transactionProcessing(msg);
        } catch (RuntimeException e) {
            // expected
        }
        verify(producerService, never()).sendWithWait(any());
    }
}
