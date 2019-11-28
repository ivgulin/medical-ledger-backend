package com.mokujin.user.model.document.impl.medical.hl7.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeableConcept implements Serializable {

    private List<Coding> coding;

    private String text;

}
