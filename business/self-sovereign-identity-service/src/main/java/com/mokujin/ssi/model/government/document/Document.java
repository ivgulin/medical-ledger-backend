package com.mokujin.ssi.model.government.document;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mokujin.ssi.model.government.document.impl.Certificate;
import com.mokujin.ssi.model.government.document.impl.Diploma;
import com.mokujin.ssi.model.government.document.impl.NationalNumber;
import com.mokujin.ssi.model.government.document.impl.NationalPassport;
import lombok.AllArgsConstructor;
import lombok.Data;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXISTING_PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = NAME, property = "type", include = EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = NationalPassport.class, name = "passport"),
        @JsonSubTypes.Type(value = NationalNumber.class, name = "number"),
        @JsonSubTypes.Type(value = Diploma.class, name = "diploma"),
        @JsonSubTypes.Type(value = Certificate.class, name = "certificate")
})
public abstract class Document {

    private String type;

    public enum Type{
        passport,
        diploma,
        certificate,
        number
    }
}
