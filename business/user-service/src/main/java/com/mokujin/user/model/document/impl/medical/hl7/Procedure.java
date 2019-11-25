package com.mokujin.user.model.document.impl.medical.hl7;

import com.mokujin.user.model.document.Document;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class Procedure extends Document {

    // TODO: 11/25/2019 start this 


    public Procedure() {
        super(MedicalDocumentType.Procedure.name());
    }
}
