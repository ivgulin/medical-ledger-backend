package com.mokujin.ssi.model.government.document.impl;

import com.mokujin.ssi.model.government.document.NationalDocument;
import lombok.Data;

@Data
public class NationalNumber implements NationalDocument {

    private String number;

    private Long registrationDate;

    private String issuer;

}
