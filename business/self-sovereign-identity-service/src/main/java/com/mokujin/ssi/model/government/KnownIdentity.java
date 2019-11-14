package com.mokujin.ssi.model.government;

import com.mokujin.ssi.model.government.document.impl.NationalNumber;
import com.mokujin.ssi.model.government.document.impl.NationalPassport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnownIdentity {

    private NationalPassport nationalPassport;

    private NationalNumber nationalNumber;
}
