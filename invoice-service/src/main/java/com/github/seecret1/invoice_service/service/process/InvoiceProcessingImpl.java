package com.github.seecret1.invoice_service.service.process;

import com.github.seecret1.invoice_service.config.SpendingAndFreeLimitsConfig;
import com.github.seecret1.invoice_service.config.kafka.properties.KafkaProperties;
import com.github.seecret1.invoice_service.dto.message.BaseMessage;
import com.github.seecret1.invoice_service.dto.order.OrderInvoiceDto;
import com.github.seecret1.invoice_service.dto.request.CardInvoiceCreateRequest;
import com.github.seecret1.invoice_service.dto.response.CardInvoiceResponse;
import com.github.seecret1.invoice_service.entity.enums.CardType;
import com.github.seecret1.invoice_service.entity.enums.OrderStatus;
import com.github.seecret1.invoice_service.exception.InvoiceNotFoundException;
import com.github.seecret1.invoice_service.kafka.producer.OrderMessageKafkaProducerService;
import com.github.seecret1.invoice_service.mapper.CardInvoiceMapper;
import com.github.seecret1.invoice_service.service.CardInvoiceService;
import com.github.seecret1.invoice_service.utils.DocumentType;
import com.github.seecret1.invoice_service.utils.InvoiceNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceProcessingImpl implements InvoiceProcessing {

    private final SpendingAndFreeLimitsConfig spendingAndFreeLimitsConfig;

    private final CardInvoiceService cardInvoiceService;

    private final OrderMessageKafkaProducerService orderMessageKafkaProducerService;

    private final KafkaProperties kafkaProperties;

    private final CardInvoiceMapper cardInvoiceMapper;

    @Override
    public void processOrder(OrderInvoiceDto request) {
        List<CardInvoiceResponse> invoice;
        BaseMessage message = new BaseMessage();
        try {
            request.validate();
            invoice = cardInvoiceService.findByUserId(request.getUserId());

            if (invoice == null) {
                throw new InvoiceNotFoundException("Invoice not found by card ID: " + request.getCardId());
            }
            message = cardInvoiceMapper.toMessageList(request, invoice, OrderStatus.SUCCESS, "Successfully founded invoice");
        } catch (InvoiceNotFoundException ex) {
            log.error("Invoice not found", ex);
            message.setMessage("Invoice not found" + ex.getMessage());
        } catch (Exception ex) {
            message.setMessage("Error processing request" + ex.getMessage());
            throw ex;
        }
        orderMessageKafkaProducerService.sendWithWait(kafkaProperties.getResponseOrdersTopic(), message);
    }

    @Override
    public void createInvoice(OrderInvoiceDto request) {
        BaseMessage message = new BaseMessage();
        try {
            request.validate();
            var invoice = cardInvoiceService.create(
                    new CardInvoiceCreateRequest( //TODO: вынести в маппер
                            request.getCardId(),
                            request.getUserId(),
                            InvoiceNumberGenerator.generateWithPrefix(getDocumentTypeWithCardType(request.getCardType()).name()),
                            request.getCurrency() != null ? request.getCurrency() : "RUB", //TODO: вынести в утилиту на время (пока не задана сущность валюты)
                            request.getBalance(),
                            spendingAndFreeLimitsConfig.getMaxLimitForType(request.getCardType()),
                            spendingAndFreeLimitsConfig.getCommissionLimitForType(request.getCardType())
                    )
            );
            message = cardInvoiceMapper.toMessage(request, invoice, OrderStatus.SUCCESS, "Successfully created invoice");
        } catch (Exception ex) {
            message.setMessage("Error processing request" + ex.getMessage());
            throw ex;
        }
        orderMessageKafkaProducerService.sendWithWait(kafkaProperties.getTranslateTopic(), message);
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
