package com.github.seecret1.userservice.model;

import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.userservice.entity.RoleType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserFilterModel {

    @NotNull(message = "Page must be set!")
    @Builder.Default
    private PageModel page = new PageModel(0, 10);

    private String firstName;

    private String lastName;

    private String middleName;

    private LocalDate birthDate;

    private RoleType role;

    private boolean deleted;
}
