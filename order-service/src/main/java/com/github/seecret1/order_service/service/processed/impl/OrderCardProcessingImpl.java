package com.github.seecret1.order_service.service.processed.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.order_service.dto.BaseMessage;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.dto.invoice.CardInvoiceResponse;
import com.github.seecret1.order_service.entity.enums.CardReceivingMethod;
import com.github.seecret1.order_service.entity.enums.CardType;
import com.github.seecret1.order_service.entity.enums.OrderType;
import com.github.seecret1.order_service.exception.OrderTypeException;
import com.github.seecret1.order_service.service.OrderMessagesService;
import com.github.seecret1.order_service.service.processed.OrderCardProcessing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCardProcessingImpl implements OrderCardProcessing {

    private final OrderMessagesService orderMessagesService;

    private final ObjectMapper objectMapper;

    //TODO: добавить метрики
    @Override
    public void processOrder(OrderCardDto event) {
        try {
            if (event.getOrderType() != OrderType.CARD) { //TODO: в данный момент произведена только работа с картами
                throw new OrderTypeException("Order card service works only with OrderType=CARD");
            }

            event.validate();
            var invoice = orderMessagesService.saveOrder(event);
            log.debug("Sending request in invoice-service: {}", invoice);

        } catch (Exception ex) { //TODO: выбрасывать в Prometheus
            log.error("Error processing order: traceId={}, error={}", event.getTraceId(), ex.getMessage(), ex);
            orderMessagesService.processingError(event, ex);
        }
    }

    @Override
    public void processMessage(BaseMessage message) {
        List<CardInvoiceResponse> invoiceResponses = extractInvoices(message.getData());
        log.info("Invoice responses List: {}", invoiceResponses.size());
        orderMessagesService.processingInvoice(message, invoiceResponses);
    }

    @Override
    public void processMessageOnInvoiceService(BaseMessage message) {
        orderMessagesService.processingResponseInCardService(message);
    }

    private List<CardInvoiceResponse> extractInvoices(Object data) {
        if (data == null) {
            return Collections.emptyList();
        }

        try {
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (list.isEmpty()) {
                    return Collections.emptyList();
                }

                Object first = list.get(0);
                if (first instanceof CardInvoiceResponse) {
                    return (List<CardInvoiceResponse>) list;
                }

                return objectMapper.convertValue(data,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, CardInvoiceResponse.class));
            }
            CardInvoiceResponse single = objectMapper.convertValue(data, CardInvoiceResponse.class);
            return List.of(single);

        } catch (Exception ex) {
            log.error("Failed to parse invoice data: {}", data, ex);
            return Collections.emptyList();
        }
    }
}
