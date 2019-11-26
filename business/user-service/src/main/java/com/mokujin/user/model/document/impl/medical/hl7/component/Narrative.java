package com.mokujin.user.model.document.impl.medical.hl7.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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