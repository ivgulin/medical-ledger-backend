package com.mokujin.user.model;

import lombok.Data;

import java.util.Date;

@Data
public class UserRegistrationDetails {

    private String nationalNumber;

    private String firstName;

    private String lastName;

    private String fatherName;

    private Date dateOfBirth;

}
