package com.mokujin.ssi.model.government.document.impl;

import com.mokujin.ssi.model.government.document.NationalDocument;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NationalNumber extends NationalDocument {

    private String number;

    private Long registrationDate;

    private String issuer;

    public NationalNumber(String number, Long registrationDate, String issuer) {
        super(Type.number.name());
        this.number = number;
        this.registrationDate = registrationDate;
        this.issuer = issuer;
    }

    public NationalNumber() {
        super(Type.number.name());
    }

}
