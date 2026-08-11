package com.github.seecret1.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для ответа с информацией о заказе
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    /**
     * ID заказа
     */
    private Long id;

    /**
     * ID пользователя
     */
    private String userId;

    /**
     * Название товара
     */
    private String productName;

    /**
     * Количество товара
     */
    private Integer quantity;

    /**
     * Цена за единицу
     */
    private BigDecimal price;

    /**
     * Общая сумма заказа (price * quantity)
     */
    private BigDecimal totalAmount;

    /**
     * Статус заказа (PENDING, PROCESSING, COMPLETED, CANCELLED, etc.)
     */
    private String status;

    /**
     * Описание заказа
     */
    private String description;

    /**
     * Теги заказа
     */
    private List<String> tags;

    /**
     * Адрес доставки
     */
    private String deliveryAddress;

    /**
     * Способ оплаты
     */
    private String paymentMethod;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;

    /**
     * Дата последнего обновления
     */
    private LocalDateTime updatedAt;

    /**
     * Дата доставки (если выполнено)
     */
    private LocalDateTime deliveredAt;

    /**
     * Промокод (если применен)
     */
    private String promoCode;

    /**
     * Скидка в процентах
     */
    private BigDecimal discountPercent;

    /**
     * Итоговая сумма со скидкой
     */
    private BigDecimal finalAmount;
}