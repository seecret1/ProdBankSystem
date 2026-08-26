package com.github.seecret1.invoice_service.service.process;

import com.github.seecret1.invoice_service.config.SpendingLimitsConfig;
import com.github.seecret1.invoice_service.dto.order.BaseMessage;
import com.github.seecret1.invoice_service.dto.order.OrderInvoiceDto;
import com.github.seecret1.invoice_service.dto.request.CardInvoiceCreateRequest;
import com.github.seecret1.invoice_service.dto.response.CardInvoiceResponse;
import com.github.seecret1.invoice_service.entity.enums.CardType;
import com.github.seecret1.invoice_service.entity.enums.OrderStatus;
import com.github.seecret1.invoice_service.exception.InvoiceNotFoundException;
import com.github.seecret1.invoice_service.kafka.producer.KafkaProducerService;
import com.github.seecret1.invoice_service.mapper.CardInvoiceMapper;
import com.github.seecret1.invoice_service.service.CardInvoiceService;
import com.github.seecret1.invoice_service.utils.DocumentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceProcessed { //TODO: добавить интерфейс

    private final SpendingLimitsConfig spendingLimitsConfig;

    private final CardInvoiceService cardInvoiceService;

    private final KafkaProducerService kafkaProducerService;

    private final CardInvoiceMapper cardInvoiceMapper;

    public BaseMessage processOrder(OrderInvoiceDto request) {
        List<CardInvoiceResponse> invoice;
        BaseMessage message = new BaseMessage();
        try {
            request.validate();
            invoice = cardInvoiceService.findByUserId(request.getUserId());

            if (invoice == null) {
                throw new InvoiceNotFoundException("Invoice not found by card ID: " + request.getCardId());
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

    public BaseMessage createInvoice(OrderInvoiceDto request) {
        BaseMessage message = new BaseMessage();
        try {
            request.validate();
            var invoice = cardInvoiceService.create(
                    new CardInvoiceCreateRequest( //TODO: вынести в маппер
                            request.getCardId(),
                            request.getUserId(),
                            getDocumentTypeWithCardType(request.getCardType()).name(),
                            request.getCurrency() != null ? request.getCurrency() : "RUB", //TODO: вынести в утилиту на время (пока не задана сущность валюты)
                            request.getBalance(),
                            spendingLimitsConfig.getMaxLimitForType(request.getCardType())
                    )
            );
            message = cardInvoiceMapper.toMessage(request, invoice, OrderStatus.PENDING, "Successfully founded invoice");
        } catch (Exception ex) {
            message.setMessage("Error processing request" + ex.getMessage());
            throw ex;
        }
        kafkaProducerService.sendWithWait(message);
        return message;
    }

    private DocumentType getDocumentTypeWithCardType(CardType cardType) {
        switch(cardType) {
            case CREDIT:
                return DocumentType.CRC;
            case DEBIT_PERSONAL:
                return DocumentType.DBC;
            case DEBIT:
                return DocumentType.DBC;
        }
        return null;
    }
}
