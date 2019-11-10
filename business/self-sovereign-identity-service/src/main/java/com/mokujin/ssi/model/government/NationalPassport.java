package com.mokujin.ssi.model.government;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class NationalPassport extends Document {

    private String firstName;

    private String lastName;

    private String fatherName;

    private LocalDate dateOfBirth;

    private String placeOfBirth;

    private String image;

    private String sex;

    private String issuer;

    private LocalDate dateOfIssue;

    private Set<PlaceOfResidence> placesOfResidence;

}
