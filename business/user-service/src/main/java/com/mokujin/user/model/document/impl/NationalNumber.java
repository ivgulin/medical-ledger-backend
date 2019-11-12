package com.mokujin.user.model.document.impl;

import com.mokujin.user.model.document.NationalDocument;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NationalNumber implements NationalDocument {

    private String number;

    private LocalDate registrationDate;

    private String issuer;

}
