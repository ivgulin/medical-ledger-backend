package com.mokujin.user.model.document.impl;

import com.mokujin.user.model.document.NationalDocument;
import lombok.Data;

@Data
public class Diploma extends NationalDocument {

    private String number;

    private String firstName;

    private String lastName;

    private String fatherName;

    private String placeOfStudy;

    private String courseOfStudy;

    private Long dateOfIssue;

    private String qualification;

    private String issuer;

    public Diploma(String number, String firstName, String lastName, String fatherName, String placeOfStudy,
                   String courseOfStudy, Long dateOfIssue, String qualification, String issuer) {
        super(Type.diploma.name());
        this.number = number;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fatherName = fatherName;
        this.placeOfStudy = placeOfStudy;
        this.courseOfStudy = courseOfStudy;
        this.dateOfIssue = dateOfIssue;
        this.qualification = qualification;
        this.issuer = issuer;
    }

    public Diploma() {
        super(Type.diploma.name());
    }
}
