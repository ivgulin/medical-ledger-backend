package com.mokujin.user.model.document.impl.medical.hl7.component;

import lombok.Data;

@Data
public class Narrative {

    private NarrativeStatus status;

    private String div;

    public enum NarrativeStatus {
        generated,
        extensions,
        additional,
        empty
    }
}