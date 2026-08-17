package com.github.seecret1.delivery_service.mapper;

import com.github.seecret1.delivery_service.dto.user.FullNameDto;
import com.github.seecret1.delivery_service.dto.user.RecipientDto;
import com.github.seecret1.delivery_service.entity.FullName;
import com.github.seecret1.delivery_service.entity.Recipient;
import com.github.seecret1.delivery_service.entity.enums.PersonType;
import com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RecipientMapper Unit Tests")
class RecipientMapperTest {

    private RecipientMapper recipientMapper;

    @BeforeEach
    void setUp() {
        recipientMapper = new RecipientMapperImpl();
    }

    @Test
    @DisplayName("Should map RecipientDto to Recipient entity")
    void shouldMapDtoToEntity() {
        RecipientDto dto = DeliveryTestDataFactory.defaultRecipientDto();

        Recipient result = recipientMapper.toEntity(dto);

        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getContactPhone()).isEqualTo(dto.getContactPhone());
        assertThat(result.getOfficeId()).isEqualTo(dto.getOfficeId());
        assertThat(result.getPersonType()).isEqualTo(PersonType.PHYSICAL);
        assertThat(result.getFullName().getFirstName()).isEqualTo("Ivan");
        assertThat(result.getFullName().getLastName()).isEqualTo("Petrov");
        assertThat(result.getFullName().getMiddleName()).isEqualTo("Sergeevich");
        assertThat(result.getDeleted()).isFalse();
    }

    @Test
    @DisplayName("Should map Recipient entity to RecipientDto")
    void shouldMapEntityToDto() {
        Recipient entity = DeliveryTestDataFactory.defaultRecipient();

        RecipientDto result = recipientMapper.toDto(entity);

        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getContactPhone()).isEqualTo(entity.getContactPhone());
        assertThat(result.getOfficeId()).isEqualTo(entity.getOfficeId());
        assertThat(result.getPersonType()).isEqualTo(PersonType.PHYSICAL);
        assertThat(result.getFullName().getFirstName()).isEqualTo("Ivan");
        assertThat(result.getFullName().getLastName()).isEqualTo("Petrov");
        assertThat(result.getFullName().getMiddleName()).isEqualTo("Sergeevich");
    }

    @Test
    @DisplayName("Should map FullNameDto to FullName entity")
    void shouldMapFullNameDtoToEntity() {
        FullNameDto dto = FullNameDto.builder()
                .firstName("Anna")
                .lastName("Smirnova")
                .middleName(null)
                .build();

        FullName result = recipientMapper.toFullName(dto);

        assertThat(result.getFirstName()).isEqualTo("Anna");
        assertThat(result.getLastName()).isEqualTo("Smirnova");
        assertThat(result.getMiddleName()).isNull();
    }

    @Test
    @DisplayName("Should map FullName entity to FullNameDto")
    void shouldMapFullNameEntityToDto() {
        FullName entity = FullName.builder()
                .firstName("Anna")
                .lastName("Smirnova")
                .middleName(null)
                .build();

        FullNameDto result = recipientMapper.toFullNameDto(entity);

        assertThat(result.getFirstName()).isEqualTo("Anna");
        assertThat(result.getLastName()).isEqualTo("Smirnova");
        assertThat(result.getMiddleName()).isNull();
    }

    @Test
    @DisplayName("Should map recipient with LEGAL person type")
    void shouldMapLegalPersonType() {
        RecipientDto dto = DeliveryTestDataFactory.defaultRecipientDto();
        dto.setPersonType(PersonType.LEGAL);

        Recipient result = recipientMapper.toEntity(dto);

        assertThat(result.getPersonType()).isEqualTo(PersonType.LEGAL);
    }

    @Test
    @DisplayName("Should map recipient with null office id")
    void shouldMapNullOfficeId() {
        RecipientDto dto = DeliveryTestDataFactory.defaultRecipientDto();
        dto.setOfficeId(null);

        Recipient result = recipientMapper.toEntity(dto);

        assertThat(result.getOfficeId()).isNull();
    }
}
