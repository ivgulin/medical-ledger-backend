package com.mokujin.ssi.model.government.document;

import com.mokujin.ssi.model.document.national.NationalDocument;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
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
        super(NationalDocumentType.Diploma.name());
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
        super(NationalDocumentType.Diploma.name());
    }
}
