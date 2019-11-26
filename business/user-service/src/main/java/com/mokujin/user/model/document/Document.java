package com.mokujin.user.model.document;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mokujin.user.model.document.impl.medical.dicom.MedicalImage;
import com.mokujin.user.model.document.impl.medical.hl7.Procedure;
import com.mokujin.user.model.document.impl.national.Certificate;
import com.mokujin.user.model.document.impl.national.Diploma;
import com.mokujin.user.model.document.impl.national.NationalNumber;
import com.mokujin.user.model.document.impl.national.NationalPassport;
import lombok.AllArgsConstructor;
import lombok.Data;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXISTING_PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = NAME, property = "resourceType", include = EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = NationalPassport.class, name = "Passport"),
        @JsonSubTypes.Type(value = Diploma.class, name = "Diploma"),
        @JsonSubTypes.Type(value = Certificate.class, name = "Certificate"),
        @JsonSubTypes.Type(value = NationalNumber.class, name = "Number"),
        @JsonSubTypes.Type(value = Procedure.class, name = "Procedure"),
        @JsonSubTypes.Type(value = MedicalImage.class, name = "MedicalImage")
})
public class Document {

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