package com.github.seecret1.order_service.service.impl;

import com.github.seecret1.order_service.dto.OrderMessage;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.entity.OrderCard;
import com.github.seecret1.order_service.entity.OrderType;
import com.github.seecret1.order_service.exception.OrderTypeException;
import com.github.seecret1.order_service.feign.UserServiceClient;
import com.github.seecret1.order_service.kafka.OrderKafkaProducerService;
import com.github.seecret1.order_service.mapper.OrderCardManualMapper;
import com.github.seecret1.order_service.repository.OrderCardRepository;
import com.github.seecret1.order_service.service.OrderCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCardServiceImpl implements OrderCardService {

    private final OrderCardRepository orderCardRepository;

    private final OrderKafkaProducerService orderKafkaProducerService;

    @Override
    @Transactional
    public OrderMessage createOrder(OrderCardDto event) {

        // TODO: Добавить обработку и работу со статусами
        if (event.getOrderType() == OrderType.CARD) {
            event.validate();
        }
        else throw new OrderTypeException("Order card service works only with OrderType=CARD");

        // TODO: добавить проверку счетов карт (если имеется задолженность -> REJECT)

        // TODO: если:
        //  тип полчения = в офисe (возможно:
        //  1) что нет офисов в данном городе (REJECT)
        //  2) офисы закрыты на какое-то время (REJECT)
        //  3) нужно сначала осуществить доставку карты в офис [при type=DEBIT_PROFILE])
        //  тип полечния = доставка -> правизываем работу с delivery-service
        //  Примеры:
        //  Работа с delivery-service: (возможно доставка по данному адресу не осуществляется в данный момент)
        //  Возможно доставка в целом недоступна из-за нагрузки

        // TODO: продумать логику со статусом ERROR

        OrderCard order = OrderCardManualMapper.toEntity(event);
        orderCardRepository.save(order);

        var message = OrderCardManualMapper.toMessage(order);
        orderKafkaProducerService.sendWithWait(event, message);

        log.info("Response sent to Kafka: order ID={}", order.getId());
        return message;
    }
}
