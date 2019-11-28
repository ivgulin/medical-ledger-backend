package com.mokujin.ssi.model.government;

import com.mokujin.ssi.model.government.document.Certificate;
import com.mokujin.ssi.model.government.document.Diploma;
import com.mokujin.ssi.model.government.document.NationalNumber;
import com.mokujin.ssi.model.government.document.NationalPassport;
import com.mokujin.ssi.model.internal.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnownIdentity {

    private Role role;

    private NationalPassport nationalPassport;

    private NationalNumber nationalNumber;

    private Diploma diploma;

    private List<Certificate> certificates = new ArrayList<>();

}
