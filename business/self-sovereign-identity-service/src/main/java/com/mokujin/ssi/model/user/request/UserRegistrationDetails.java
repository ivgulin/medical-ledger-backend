package com.mokujin.ssi.model.user.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDetails {

    private String nationalNumber;

    private String firstName;

    private String lastName;

    private String fatherName;

    private Long dateOfBirth;

}
