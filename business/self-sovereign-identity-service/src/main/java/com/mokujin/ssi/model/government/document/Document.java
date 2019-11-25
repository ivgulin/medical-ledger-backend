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
@JsonTypeInfo(use = NAME, property = "resourceType", include = EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = NationalPassport.class, name = "Passport"),
        @JsonSubTypes.Type(value = NationalNumber.class, name = "Number"),
        @JsonSubTypes.Type(value = Diploma.class, name = "Diploma"),
        @JsonSubTypes.Type(value = Certificate.class, name = "Certificate")
})
public abstract class Document {

    private String resourceType;

    public enum NationalDocumentType {
        Passport,
        Diploma,
        Certificate,
        Number
    }
}
