package com.mokujin.ssi.model.government;

import lombok.Data;

import javax.print.Doc;
import java.time.LocalDate;

@Data
public class NationalNumber extends NationalDocument {

    private String number;

    private LocalDate registrationDate;

    private String issuer;

}
