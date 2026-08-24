package com.github.seecret1.invoice_service.service.process;

import com.github.seecret1.invoice_service.dto.order.BaseMessage;
import com.github.seecret1.invoice_service.dto.order.OrderCardDto;
import com.github.seecret1.invoice_service.dto.response.CardInvoiceResponse;
import com.github.seecret1.invoice_service.entity.enums.OrderStatus;
import com.github.seecret1.invoice_service.exception.InvoiceNotFoundException;
import com.github.seecret1.invoice_service.kafka.producer.KafkaProducerService;
import com.github.seecret1.invoice_service.mapper.CardInvoiceMapper;
import com.github.seecret1.invoice_service.service.CardInvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceProcessed {

    private final CardInvoiceService cardInvoiceService;

    private final KafkaProducerService kafkaProducerService;

    private final CardInvoiceMapper cardInvoiceMapper;

    public BaseMessage processOrder(OrderCardDto request) {
        CardInvoiceResponse invoice;
        BaseMessage message = new BaseMessage();
        try {
            invoice = cardInvoiceService.findById(request.getInvoiceId());

            if (invoice == null) {
                throw new InvoiceNotFoundException("Invoice not found for id: " + request.getInvoiceId());
            }
            message = cardInvoiceMapper.toMessage(request, invoice, OrderStatus.PENDING, "Successfully founded invoice");
        } catch (InvoiceNotFoundException ex) {
            log.error("Invoice not found", ex);
            message.setMessage("Invoice not found" + ex.getMessage());
        } catch (Exception ex) {
            message.setMessage("Error processing request" + ex.getMessage());
            throw ex;
        }
        kafkaProducerService.sendWithWait(message);
        return message;
    }
}
