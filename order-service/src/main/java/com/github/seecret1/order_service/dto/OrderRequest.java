package com.github.seecret1.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO для создания/обновления заказа
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    /**
     * ID пользователя (опционально, может быть в заголовке X-User-Id)
     */
    private String userId;

    /**
     * Название товара
     */
    @NotBlank(message = "Название товара обязательно")
    @Size(max = 255, message = "Название товара не должно превышать 255 символов")
    private String productName;

    /**
     * Количество товара
     */
    @NotNull(message = "Количество обязательно")
    @Positive(message = "Количество должно быть положительным числом")
    private Integer quantity;

    /**
     * Цена за единицу товара
     */
    @NotNull(message = "Цена обязательна")
    @Positive(message = "Цена должна быть положительным числом")
    private BigDecimal price;

    /**
     * Описание заказа (опционально)
     */
    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    /**
     * Теги для заказа (опционально)
     */
    @Size(max = 10, message = "Не более 10 тегов")
    private List<@NotBlank String> tags;

    /**
     * Адрес доставки (опционально)
     */
    private String deliveryAddress;

    /**
     * Способ оплаты (опционально)
     */
    private String paymentMethod;
}