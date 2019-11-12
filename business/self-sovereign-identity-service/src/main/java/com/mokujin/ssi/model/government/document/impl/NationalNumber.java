package com.mokujin.ssi.model.government.document.impl;

import com.mokujin.ssi.model.government.document.NationalDocument;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NationalNumber implements NationalDocument {

    private String number;

    private LocalDate registrationDate;

    private String issuer;

}
