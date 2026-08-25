package com.github.seecret1.cardservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.cardservice.dto.order.OrderStatus;
import com.github.seecret1.cardservice.dto.order.message.BaseMessage;
import com.github.seecret1.cardservice.dto.order.message.OrderCardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final CardService service;

    private final ObjectMapper objectMapper;

    public void orderProcessing(BaseMessage order) {
        String cardId = order.getProductId();
        OrderStatus status = order.getStatus();
        OrderCardResponse orderCardResponse = objectMapper.convertValue(order, OrderCardResponse.class);

        log.info("Processing order with status: {} for card: {}", status, cardId);

        switch (status) {
            case SUCCESS:
                service.activateCard(cardId, orderCardResponse.getInvoiceId());
                sendNotification();
                break;

            case PENDING:
                // TODO: Добавить работу с retry топиком
                sendNotification();
                break;

            case REJECTED:
                sendNotification();
                break;

            case ERROR:
//                kafkaSenderDlt.sendMessageInDlt(order); TODO: заретраить
                sendNotification();
                break;

            default:
                log.warn("Unknown order status: {} for order: {}", status, order.getOrderId());
                break;
        }
    }

    private void sendNotification() {
        //TODO: Добавить обработку 1, затем заменить на 2:
        // 1) Отправить сообщение по email (SMTP)
        // 2) Добавить работу с notification-service
        log.info("Send message to person");
    }
}
