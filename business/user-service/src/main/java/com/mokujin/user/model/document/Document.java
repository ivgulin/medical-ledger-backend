package com.mokujin.user.model.document;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mokujin.user.model.document.impl.Certificate;
import com.mokujin.user.model.document.impl.Diploma;
import com.mokujin.user.model.document.impl.NationalNumber;
import com.mokujin.user.model.document.impl.NationalPassport;
import lombok.AllArgsConstructor;
import lombok.Data;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXISTING_PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = NAME, property = "type", include = EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = NationalPassport.class, name = "passport"),
        @JsonSubTypes.Type(value = Diploma.class, name = "diploma"),
        @JsonSubTypes.Type(value = Certificate.class, name = "certificate"),
        @JsonSubTypes.Type(value = NationalNumber.class, name = "number")
})
public class Document {

    private String type;

    public enum Type {
        passport,
        diploma,
        certificate,
        number
    }

}