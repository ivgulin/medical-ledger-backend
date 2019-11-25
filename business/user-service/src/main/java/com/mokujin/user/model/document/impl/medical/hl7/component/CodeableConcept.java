package com.mokujin.user.model.document.impl.medical.hl7.component;

import lombok.Data;

import java.util.List;

@Data
public class CodeableConcept {

    private List<Coding> coding;

    private String text;

}
