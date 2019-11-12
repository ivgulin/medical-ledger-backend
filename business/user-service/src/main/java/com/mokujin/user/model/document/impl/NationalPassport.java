package com.mokujin.user.model.document.impl;

import com.mokujin.user.model.document.NationalDocument;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NationalPassport implements NationalDocument {

    private String firstName;

    private String lastName;

    private String fatherName;

    private LocalDate dateOfBirth;

    private String placeOfBirth;

    private String image;

    private String sex;

    private String issuer;

    private LocalDate dateOfIssue;

}
