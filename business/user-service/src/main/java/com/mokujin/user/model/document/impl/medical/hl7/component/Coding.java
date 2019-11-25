package com.mokujin.user.model.document.impl.medical.hl7.component;

import lombok.Data;

@Data
public class Coding {

    private String system;

    private String version;

    private String code;

    private String display;
    
}