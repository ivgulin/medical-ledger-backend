package com.mokujin.user.model.internal;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MedicalImageDraft extends DocumentDraft {

    private String image;

    public MedicalImageDraft() {
        super(Type.MedicalImage.name());
    }
}
