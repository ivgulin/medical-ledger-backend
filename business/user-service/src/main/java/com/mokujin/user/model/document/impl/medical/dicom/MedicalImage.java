package com.mokujin.user.model.document.impl.medical.dicom;

import com.mokujin.user.model.document.Document;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class MedicalImage extends Document {

    Map<String, String> attributes;

    public MedicalImage(Map<String, String> attributes) {
        super(MedicalDocumentType.MedicalImage.name());
        this.attributes = attributes;
    }

    public MedicalImage() {
        super(MedicalDocumentType.MedicalImage.name());
    }
}
