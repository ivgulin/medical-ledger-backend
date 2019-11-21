package com.mokujin.user.model.presentation;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PresentationAttributes {

    private List<String> passportAttributes = new ArrayList<>();
    private List<String> nationalNumberAttributes = new ArrayList<>();
    private List<String> diplomaAttributes = new ArrayList<>();
    private List<String> certificateAttributes = new ArrayList<>();

    public void addPassportAttribute(String attribute) {
        passportAttributes.add(attribute);
    }

    public void addNationalNumberAttribute(String attribute) {
        nationalNumberAttributes.add(attribute);
    }

    public void addDiplomaAttribute(String attribute) {
        diplomaAttributes.add(attribute);
    }

    public void addCertificateAttribute(String attribute) {
        certificateAttributes.add(attribute);
    }
}
