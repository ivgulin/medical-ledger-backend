package com.mokujin.ssi.model.government;

import com.mokujin.ssi.model.government.document.impl.Certificate;
import com.mokujin.ssi.model.government.document.impl.Diploma;
import com.mokujin.ssi.model.government.document.impl.NationalNumber;
import com.mokujin.ssi.model.government.document.impl.NationalPassport;
import com.mokujin.ssi.model.internal.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnownIdentity {

    private Role role;

    private NationalPassport nationalPassport;

    private NationalNumber nationalNumber;

    private Diploma diploma;

    private List<Certificate> certificates;

}
