package com.mokujin.government.model.dto;

import com.mokujin.government.model.entity.NationalNumber;
import lombok.Data;

@Data
public class NationalNumberDTO extends NationalNumber {

    private String resourceType;

    public NationalNumberDTO(NationalNumber nationalNumber) {
        super(nationalNumber.getNumber(), nationalNumber.getRegistrationDate(), nationalNumber.getIssuer());
        this.resourceType = DocumentType.Number.name();
    }
}
