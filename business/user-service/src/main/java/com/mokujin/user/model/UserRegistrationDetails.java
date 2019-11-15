package com.mokujin.user.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class UserRegistrationDetails {

    private String nationalNumber;

    private String firstName;

    private String lastName;

    private String fatherName;

    private Date dateOfBirth;

}
