package com.mokujin.user.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@NoArgsConstructor
public class UserRegistrationDetails {

    @NotNull(message = "Please provide your national number")
    private String nationalNumber;

    @NotNull(message = "Please provide your first name")
    private String firstName;

    @NotNull(message = "Please provide your last name")
    private String lastName;

    @NotNull(message = "Please provide your father's name")
    private String fatherName;

    @NotNull(message = "Please provide your date of birth")
    private Date dateOfBirth;

}
