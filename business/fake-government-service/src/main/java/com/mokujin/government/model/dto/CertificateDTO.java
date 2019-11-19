package com.mokujin.government.model.dto;

import com.mokujin.government.model.entity.Certificate;
import lombok.Data;

@Data
public class CertificateDTO extends Certificate {

    private String type;

    public CertificateDTO(Certificate certificate) {
        super(certificate.getId(), certificate.getNumber(), certificate.getFirstName(), certificate.getLastName(),
                certificate.getFatherName(), certificate.getDateOfExam(), certificate.getDateOfIssue(),
                certificate.getQualification(), certificate.getCourseOfStudy(), certificate.getCategory(),
                certificate.getExpiresIn(), certificate.getIssuer(), certificate.getKnownIdentity());
        this.type = "certificate";
    }
}
