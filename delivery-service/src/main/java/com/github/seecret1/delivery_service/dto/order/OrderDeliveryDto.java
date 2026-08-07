package com.github.seecret1.delivery_service.dto.order;

import com.github.seecret1.delivery_service.dto.address.AddressRequest;
import com.github.seecret1.delivery_service.dto.user.FullNameDto;
import com.github.seecret1.delivery_service.entity.enums.PersonType;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
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
        if (fullName.getFirstName() == null || fullName.getLastName() == null) {
            throw new ValidationException("Must not be null full name fields");
        }
    }
}
