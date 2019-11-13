package com.mokujin.ssi.model.government.document.impl;

import com.mokujin.ssi.model.government.document.NationalDocument;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NationalPassport implements NationalDocument {

    private String firstName;

    private String lastName;

    private String fatherName;

    private Long dateOfBirth;

    private String placeOfBirth;

    private String image;

    private String sex;

    private String issuer;

    private Long dateOfIssue;

}
