package com.mokujin.government.model.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class Person {

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private String fatherName;

    @NotNull
    private Long dateOfBirth;

    @NotNull
    private String nationalNumber;

}
