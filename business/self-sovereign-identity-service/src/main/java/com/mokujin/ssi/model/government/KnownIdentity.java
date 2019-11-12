package com.mokujin.ssi.model.government;

import com.mokujin.ssi.model.government.document.impl.NationalNumber;
import com.mokujin.ssi.model.government.document.impl.NationalPassport;
import lombok.Data;

@Data
public class KnownIdentity {

    private NationalPassport nationalPassport;

    private NationalNumber nationalNumber;
}
