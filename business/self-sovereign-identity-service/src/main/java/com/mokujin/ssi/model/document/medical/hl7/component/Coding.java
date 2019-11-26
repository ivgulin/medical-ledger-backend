package com.mokujin.ssi.model.document.medical.hl7.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coding {

    private String system;

    private String version;

    private String code;

    private String display;

}