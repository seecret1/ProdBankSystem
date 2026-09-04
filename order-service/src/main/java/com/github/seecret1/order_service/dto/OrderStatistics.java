package com.github.seecret1.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO для статистики заказов пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatistics {

    /**
     * ID пользователя
     */
    private String userId;

    /**
     * Общее количество заказов
     */
    private Long totalOrders;

    /**
     * Количество заказов в статусе PENDING
     */
    private Long pendingOrders;

    /**
     * Количество заказов в статусе PROCESSING
     */
    private Long processingOrders;

    /**
     * Количество заказов в статусе COMPLETED
     */
    private Long completedOrders;

    /**
     * Количество заказов в статусе CANCELLED
     */
    private Long cancelledOrders;

    /**
     * Количество заказов в статусе DELIVERED
     */
    private Long deliveredOrders;

    /**
     * Общая потраченная сумма (только завершенные заказы)
     */
    private BigDecimal totalSpent;

    /**
     * Средняя стоимость заказа
     */
    private BigDecimal averageOrderValue;

    /**
     * Минимальная стоимость заказа
     */
    private BigDecimal minOrderValue;

    /**
     * Максимальная стоимость заказа
     */
    private BigDecimal maxOrderValue;

    /**
     * Дата последнего заказа
     */
    private LocalDateTime lastOrderDate;

    /**
     * Дата первого заказа
     */
    private LocalDateTime firstOrderDate;

    /**
     * Количество уникальных товаров
     */
    private Long uniqueProductsCount;

    /**
     * Топ 5 самых популярных товаров
     */
    private Map<String, Long> topProducts;

    /**
     * Статистика по месяцам
     */
    private Map<String, Long> monthlyStats;

    /**
     * Среднее время между заказами (в днях)
     */
    private Double averageDaysBetweenOrders;

    /**
     * Сумма скидок (если были применены)
     */
    private BigDecimal totalDiscounts;

    /**
     * Текущий уровень пользователя (NEW, REGULAR, VIP, etc.)
     */
    private String userLevel;

    /**
     * Количество возвратов
     */
    private Long returnCount;

    /**
     * Процент возвратов
     */
    private Double returnPercentage;
}
