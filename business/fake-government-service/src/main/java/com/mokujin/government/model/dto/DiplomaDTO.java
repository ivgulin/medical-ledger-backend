package com.mokujin.government.model.dto;

import com.mokujin.government.model.entity.Diploma;
import lombok.Data;

@Data
public class DiplomaDTO extends Diploma {

    private String resourceType;

    public DiplomaDTO(Diploma diploma) {
        super(diploma.getId(), diploma.getNumber(), diploma.getFirstName(), diploma.getLastName(),
                diploma.getFatherName(), diploma.getPlaceOfStudy(), diploma.getCourseOfStudy(),
                diploma.getDateOfIssue(), diploma.getQualification(), diploma.getIssuer());
        this.resourceType = DocumentType.Diploma.name();
    }
}
