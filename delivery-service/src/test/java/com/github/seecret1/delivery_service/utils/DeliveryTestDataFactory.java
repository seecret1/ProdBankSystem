package com.github.seecret1.delivery_service.utils;

import com.github.seecret1.delivery_service.dto.address.AddressPair;
import com.github.seecret1.delivery_service.dto.address.AddressRequest;
import com.github.seecret1.delivery_service.dto.CourierDto;
import com.github.seecret1.delivery_service.dto.order.OrderCardDeliveryDto;
import com.github.seecret1.delivery_service.dto.user.FullNameDto;
import com.github.seecret1.delivery_service.dto.user.RecipientDto;
import com.github.seecret1.delivery_service.entity.Address;
import com.github.seecret1.delivery_service.entity.CardDelivery;
import com.github.seecret1.delivery_service.entity.Country;
import com.github.seecret1.delivery_service.entity.Courier;
import com.github.seecret1.delivery_service.entity.FullName;
import com.github.seecret1.delivery_service.entity.Recipient;
import com.github.seecret1.delivery_service.entity.enums.CardType;
import com.github.seecret1.delivery_service.entity.enums.DeliveryStatus;
import com.github.seecret1.delivery_service.entity.enums.OrderType;
import com.github.seecret1.delivery_service.entity.enums.PersonType;

import java.time.Instant;

public final class DeliveryTestDataFactory {

    public static final String USER_ID = "user-123";
    public static final String ORDER_ID = "order-456";
    public static final String TRACE_ID = "trace-789";
    public static final String OFFICE_ID = "office-001";
    public static final String CONTACT_PHONE = "+79991234567";

    public static final String COURIER_USER_ID = "courier-user-001";
    public static final String COURIER_ID = "courier-id-001";
    public static final String COURIER_CONTACT_PHONE = "+79998887766";

    private DeliveryTestDataFactory() {
    }

    public static FullNameDto defaultFullNameDto() {
        return FullNameDto.builder()
                .firstName("Ivan")
                .lastName("Petrov")
                .middleName("Sergeevich")
                .build();
    }

    public static FullName defaultFullName() {
        return FullName.builder()
                .firstName("Ivan")
                .lastName("Petrov")
                .middleName("Sergeevich")
                .build();
    }

    public static AddressRequest originAddressRequest() {
        return new AddressRequest("Lenina 1", "101000", "Moscow", "RU");
    }

    public static AddressRequest destinationAddressRequest() {
        return new AddressRequest("Pushkina 10", "190000", "Saint Petersburg", "RU");
    }

    public static OrderCardDeliveryDto validOrderCardDeliveryDto() {
        return OrderCardDeliveryDto.builder()
                .traceId(TRACE_ID)
                .userId(USER_ID)
                .orderId(ORDER_ID)
                .orderType(OrderType.DELIVERY)
                .createdAt(Instant.parse("2026-08-17T10:00:00Z"))
                .plannedDeliveryTime(Instant.parse("2026-08-20T12:00:00Z"))
                .cardType(CardType.DEBIT)
                .officeId(OFFICE_ID)
                .fullName(defaultFullNameDto())
                .contactPhone(CONTACT_PHONE)
                .originAddress(originAddressRequest())
                .destinationAddress(destinationAddressRequest())
                .personType(PersonType.PHYSICAL)
                .build();
    }

    public static RecipientDto defaultRecipientDto() {
        return RecipientDto.builder()
                .userId(USER_ID)
                .fullName(defaultFullNameDto())
                .contactPhone(CONTACT_PHONE)
                .officeId(OFFICE_ID)
                .personType(PersonType.PHYSICAL)
                .build();
    }

    public static Recipient defaultRecipient() {
        return Recipient.builder()
                .id(1L)
                .userId(USER_ID)
                .fullName(defaultFullName())
                .contactPhone(CONTACT_PHONE)
                .officeId(OFFICE_ID)
                .personType(PersonType.PHYSICAL)
                .deleted(false)
                .build();
    }

    public static Country defaultCountry() {
        Country country = new Country();
        country.setId(1);
        country.setCode("RU");
        country.setName("Russia");
        country.setDeleted(false);
        return country;
    }

    public static Address originAddress() {
        Address address = new Address();
        address.setId("origin-id");
        address.setAddress("Lenina 1");
        address.setZipCode("101000");
        address.setCity("Moscow");
        address.setCountry(defaultCountry());
        address.setDeleted(false);
        address.setCreatedAt(Instant.parse("2026-08-17T10:00:00Z"));
        address.setUpdatedAt(Instant.parse("2026-08-17T10:00:00Z"));
        return address;
    }

    public static Address destinationAddress() {
        Address address = new Address();
        address.setId("destination-id");
        address.setAddress("Pushkina 10");
        address.setZipCode("190000");
        address.setCity("Saint Petersburg");
        address.setCountry(defaultCountry());
        address.setDeleted(false);
        address.setCreatedAt(Instant.parse("2026-08-17T10:00:00Z"));
        address.setUpdatedAt(Instant.parse("2026-08-17T10:00:00Z"));
        return address;
    }

    public static AddressPair defaultAddressPair() {
        return new AddressPair(originAddress(), destinationAddress());
    }

    public static CardDelivery defaultCardDelivery(Recipient recipient) {
        return CardDelivery.builder()
                .id("delivery-id")
                .orderId(ORDER_ID)
                .recipient(recipient)
                .originAddress(originAddress())
                .destinationAddress(destinationAddress())
                .plannedDeliveryTime(Instant.parse("2026-08-20T12:00:00Z"))
                .cardType(CardType.DEBIT)
                .status(DeliveryStatus.CREATED)
                .deleted(false)
                .createdAt(Instant.parse("2026-08-17T10:00:00Z"))
                .build();
    }

    public static CourierDto defaultCourierDto() {
        return new CourierDto(
                COURIER_USER_ID,
                defaultFullNameDto(),
                false,
                COURIER_CONTACT_PHONE
        );
    }

    public static Courier defaultCourier() {
        return Courier.builder()
                .id(COURIER_ID)
                .userId(COURIER_USER_ID)
                .fullName(defaultFullName())
                .busy(false)
                .contactPhone(COURIER_CONTACT_PHONE)
                .deleted(false)
                .createdAt(Instant.parse("2026-08-17T10:00:00Z"))
                .updatedAt(Instant.parse("2026-08-17T10:00:00Z"))
                .build();
    }
}
