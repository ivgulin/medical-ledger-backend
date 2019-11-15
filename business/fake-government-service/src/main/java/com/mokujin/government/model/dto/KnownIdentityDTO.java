package com.mokujin.government.model.dto;

import com.mokujin.government.model.entity.KnownIdentity;
import lombok.Data;

@Data
public class KnownIdentityDTO extends KnownIdentity {

    public KnownIdentityDTO(KnownIdentity knownIdentity) {
        super(knownIdentity.getId(), knownIdentity.getNationalPassport(), knownIdentity.getNationalNumber());
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
