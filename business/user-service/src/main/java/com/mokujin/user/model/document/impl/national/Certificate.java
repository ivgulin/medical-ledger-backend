package com.mokujin.user.model.document.impl.national;

import com.mokujin.user.model.document.NationalDocument;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.mokujin.user.model.document.Document.NationalDocumentType.Certificate;

@Data
@EqualsAndHashCode(callSuper = true)
public class Certificate extends NationalDocument {

    private String number;

    private String firstName;

    private String lastName;

    private String fatherName;

    private Long dateOfExam;

    private Long dateOfIssue;

    private String qualification;

    private String courseOfStudy;

    private Category category;

    private Long expiresIn;

    private String issuer;

    public Certificate(String number, String firstName, String lastName, String fatherName, Long dateOfExam,
                       Long dateOfIssue, String qualification, String courseOfStudy, Long expiresIn, String issuer) {
        super(Certificate.name());
        this.number = number;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fatherName = fatherName;
        this.dateOfExam = dateOfExam;
        this.dateOfIssue = dateOfIssue;
        this.qualification = qualification;
        this.courseOfStudy = courseOfStudy;
        this.expiresIn = expiresIn;
        this.issuer = issuer;
    }

    public Certificate() {
        super(Certificate.name());
    }

    public enum Category {
        I, II, HIGHER
    }
}
