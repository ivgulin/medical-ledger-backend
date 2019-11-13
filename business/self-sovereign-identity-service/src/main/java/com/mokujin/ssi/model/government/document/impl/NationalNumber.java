package com.mokujin.ssi.model.government.document.impl;

import com.mokujin.ssi.model.government.document.NationalDocument;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NationalNumber implements NationalDocument {

    private String number;

    private Long registrationDate;

    private String issuer;

}
