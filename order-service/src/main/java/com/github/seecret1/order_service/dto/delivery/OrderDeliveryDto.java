package com.github.seecret1.order_service.dto.delivery;

import com.github.seecret1.order_service.dto.OrderDto;
import com.github.seecret1.order_service.dto.address.AddressRequest;
import com.github.seecret1.order_service.dto.user.FullNameDto;
import com.github.seecret1.order_service.entity.enums.PersonType;
import jakarta.validation.ValidationException;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDeliveryDto extends OrderDto {

    private String orderId;

    private String officeId;

    private FullNameDto fullName;

    private String contactPhone;

    private AddressRequest originAddress;

    private AddressRequest destinationAddress;

    private PersonType personType;

    @Override
    public void validate() {
        if (orderId == null || userId == null || traceId == null) {
            throw new ValidationException("Ids must not be null");
        }
        if (orderId.isBlank() || userId.isBlank() || traceId.isBlank()) {
            throw new ValidationException("Ids must not be blank");
        }
        if (fullName == null || orderType == null ||
                originAddress == null ||  destinationAddress == null) {
            throw new ValidationException("Must not be null fields");
        }
    }
}
