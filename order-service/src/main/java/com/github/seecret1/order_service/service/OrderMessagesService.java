package com.github.seecret1.order_service.service;

import com.github.seecret1.order_service.dto.BaseMessage;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.dto.invoice.CardInvoiceResponse;
import com.github.seecret1.order_service.dto.invoice.OrderInvoiceDto;

import java.util.List;

public interface OrderMessagesService {

    OrderInvoiceDto saveOrder(OrderCardDto message);

    void processingError(OrderCardDto message, Exception ex);

    void processingInvoice(BaseMessage message, List<CardInvoiceResponse> invoiceResponses);

    void processingResponseInCardService(BaseMessage message);
}
