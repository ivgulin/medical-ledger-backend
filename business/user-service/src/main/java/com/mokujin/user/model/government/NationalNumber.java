package com.mokujin.user.model.government;

import lombok.Data;

import java.time.LocalDate;

@Data
public class NationalNumber {

    private String number;

    private LocalDate registrationDate;

    private String issuer;

}
