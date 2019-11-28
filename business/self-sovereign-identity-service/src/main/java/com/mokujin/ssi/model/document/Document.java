package com.mokujin.ssi.model.document;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mokujin.ssi.model.document.medical.dicom.MedicalImage;
import com.mokujin.ssi.model.document.medical.hl7.Procedure;
import com.mokujin.ssi.model.government.document.Certificate;
import com.mokujin.ssi.model.government.document.Diploma;
import com.mokujin.ssi.model.government.document.NationalNumber;
import com.mokujin.ssi.model.government.document.NationalPassport;
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
        @JsonSubTypes.Type(value = Certificate.class, name = "Certificate"),
        @JsonSubTypes.Type(value = Procedure.class, name = "Procedure"),
        @JsonSubTypes.Type(value = MedicalImage.class, name = "MedicalImage")
})
public abstract class Document {

    private String resourceType;

    public enum NationalDocumentType {
        Passport,
        Diploma,
        Certificate,
        Number
    }

    public enum MedicalDocumentType {
        Procedure,
        MedicalImage
    }
}
