package com.github.seecret1.cardservice.service.processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.cardservice.dto.order.OrderStatus;
import com.github.seecret1.cardservice.dto.order.message.BaseMessage;
import com.github.seecret1.cardservice.dto.order.message.OrderCardResponse;
import com.github.seecret1.cardservice.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessingServiceImpl implements OrderProcessingService {

    private final CardService service;

    private final ObjectMapper objectMapper;

    @Override
    public void orderProcessing(BaseMessage order) {
        String cardId = order.getProductId();
        OrderStatus status = order.getStatus();
        OrderCardResponse orderCardResponse = objectMapper.convertValue(order.getData(), OrderCardResponse.class);
        log.info("Order Card Response: {}", orderCardResponse);

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
