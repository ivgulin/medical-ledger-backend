package com.mokujin.government.model.dto;

import com.mokujin.government.model.entity.NationalPassport;
import lombok.Data;

@Data
public class NationalPassportDTO extends NationalPassport {

    private String image;

    private String resourceType;

    public NationalPassportDTO(NationalPassport nationalPassport, String image) {
        super(nationalPassport.getId(), nationalPassport.getNumber(), nationalPassport.getFirstName(), nationalPassport.getLastName(),
                nationalPassport.getFatherName(), nationalPassport.getDateOfBirth(), nationalPassport.getPlaceOfBirth(),
                nationalPassport.getImageName(), nationalPassport.getSex(), nationalPassport.getIssuer(),
                nationalPassport.getDateOfIssue(), nationalPassport.getPlacesOfResidence());
        this.image = image;
        this.resourceType = DocumentType.Passport.name();
    }

    public NationalPassportDTO(NationalPassport nationalPassport) {
        super(nationalPassport.getId(), nationalPassport.getNumber(), nationalPassport.getFirstName(), nationalPassport.getLastName(),
                nationalPassport.getFatherName(), nationalPassport.getDateOfBirth(), nationalPassport.getPlaceOfBirth(),
                nationalPassport.getImageName(), nationalPassport.getSex(), nationalPassport.getIssuer(),
                nationalPassport.getDateOfIssue(), nationalPassport.getPlacesOfResidence());
        this.resourceType = DocumentType.Passport.name();
    }

}
