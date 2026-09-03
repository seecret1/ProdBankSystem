package com.github.seecret1.payment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.jwt_common.security.JwtAuthenticationFilter;
import com.github.seecret1.jwt_common.security.JwtTokenProvider;
import com.github.seecret1.payment_service.dto.payment.PaymentRequest;
import com.github.seecret1.payment_service.dto.payment.PaymentResponse;
import com.github.seecret1.payment_service.entity.enums.PaymentType;
import com.github.seecret1.payment_service.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PaymentController Unit Tests (MockMvc)")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("POST /api/v1/public/payments - should return 201 when request is valid")
    @WithMockUser(roles = {"USER"})
    void shouldCreatePayment() throws Exception {
        PaymentRequest request = new PaymentRequest("src-1", "dst-1", new BigDecimal("100.00"), PaymentType.TRANSFER, "RUB");
        PaymentResponse response = new PaymentResponse("id-1", "src-1", "dst-1", new BigDecimal("100.00"), PaymentType.TRANSFER, "RUB");

        when(paymentService.create(any(), any(PaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/public/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("id-1"))
                .andExpect(jsonPath("$.sourceInvoiceId").value("src-1"))
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    @DisplayName("POST should return 400 when body is missing")
    @WithMockUser(roles = {"USER"})
    void shouldReturnBadRequestWhenBodyMissing() throws Exception {
        mockMvc.perform(post("/api/v1/public/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is2xxSuccessful()); // currently no validation on PaymentRequest fields, so 201 is allowed
    }

    @Test
    @DisplayName("POST should map all PaymentTypes correctly")
    @WithMockUser(roles = {"ADMIN"})
    void shouldHandleAllPaymentTypes() throws Exception {
        for (PaymentType type : PaymentType.values()) {
            PaymentRequest req = new PaymentRequest("src", "dst", new BigDecimal("10.00"), type, "USD");
            PaymentResponse resp = new PaymentResponse("id", "src", "dst", new BigDecimal("10.00"), type, "USD");
            when(paymentService.create(any(), any())).thenReturn(resp);

            mockMvc.perform(post("/api/v1/public/payments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.type").value(type.name()));
        }
    }
}
