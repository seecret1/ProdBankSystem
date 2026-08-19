package com.github.seecret1.delivery_service.controller;

import com.github.seecret1.delivery_service.dto.CourierDto;
import com.github.seecret1.delivery_service.service.CourierService;
import com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory.COURIER_ID;
import static com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory.COURIER_USER_ID;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourierController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CourierController Web Tests")
class CourierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourierService courierService;

    @Test
    @DisplayName("Should create courier")
    void shouldCreateCourier() throws Exception {
        CourierDto dto = DeliveryTestDataFactory.defaultCourierDto();

        when(courierService.create(dto)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/couriers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "fullName": {"firstName": "Ivan", "lastName": "Petrov", "middleName": "Sergeevich"},
                                  "busy": false,
                                  "contactPhone": "+79998887766"
                                }
                                """.formatted(COURIER_USER_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(COURIER_USER_ID))
                .andExpect(jsonPath("$.contactPhone").value("+79998887766"));
    }

    @Test
    @DisplayName("Should return 400 when courier body is invalid")
    void shouldReturnBadRequestWhenBodyInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/couriers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": {"firstName": "Ivan", "lastName": "Petrov"},
                                  "busy": false
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return all couriers")
    void shouldFindAll() throws Exception {
        CourierDto dto = DeliveryTestDataFactory.defaultCourierDto();

        when(courierService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/couriers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(COURIER_USER_ID));
    }

    @Test
    @DisplayName("Should return first available courier")
    void shouldFindAvailable() throws Exception {
        CourierDto dto = DeliveryTestDataFactory.defaultCourierDto();

        when(courierService.findAvailable()).thenReturn(dto);

        mockMvc.perform(get("/api/v1/couriers/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(COURIER_USER_ID));
    }

    @Test
    @DisplayName("Should set courier availability")
    void shouldSetAvailability() throws Exception {
        CourierDto dto = DeliveryTestDataFactory.defaultCourierDto();

        when(courierService.setBusy(COURIER_ID, true)).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/couriers/{courierId}/availability", COURIER_ID)
                        .param("busy", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(COURIER_USER_ID));

        verify(courierService).setBusy(COURIER_ID, true);
    }

    @Test
    @DisplayName("Should delete courier")
    void shouldDeleteCourier() throws Exception {
        doNothing().when(courierService).delete(COURIER_ID);

        mockMvc.perform(delete("/api/v1/couriers/{courierId}", COURIER_ID))
                .andExpect(status().isNoContent());

        verify(courierService).delete(COURIER_ID);
    }
}