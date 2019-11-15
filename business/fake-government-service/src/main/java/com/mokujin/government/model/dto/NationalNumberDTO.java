package com.mokujin.government.model.dto;

import com.mokujin.government.model.entity.NationalNumber;
import lombok.Data;

@Data
public class NationalNumberDTO extends NationalNumber {

    private String type;

    public NationalNumberDTO(NationalNumber nationalNumber) {
        super(nationalNumber.getNumber(), nationalNumber.getRegistrationDate(), nationalNumber.getIssuer());
        this.type = "number";
    }
}
