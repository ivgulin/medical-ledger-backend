package com.mokujin.user.model.document.impl.national;

import com.mokujin.user.model.document.NationalDocument;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.mokujin.user.model.document.Document.NationalDocumentType.Passport;

@Data
@EqualsAndHashCode(callSuper = true)
public class NationalPassport extends NationalDocument {

    private String number;

    private String firstName;

    private String lastName;

    private String fatherName;

    private Long dateOfBirth;

    private String placeOfBirth;

    private String image;

    private String sex;

    private String issuer;

    private Long dateOfIssue;

    public NationalPassport(String number, String firstName, String lastName, String fatherName,
                            Long dateOfBirth, String placeOfBirth, String image, String sex, String issuer, Long dateOfIssue) {
        super(Passport.name());
        this.number = number;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fatherName = fatherName;
        this.dateOfBirth = dateOfBirth;
        this.placeOfBirth = placeOfBirth;
        this.image = image;
        this.sex = sex;
        this.issuer = issuer;
        this.dateOfIssue = dateOfIssue;
    }


    public NationalPassport() {
        super(Passport.name());
    }

}
