package com.mokujin.ssi.model.document.national;

import com.mokujin.ssi.model.document.Document;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class NationalDocument extends Document {

    public NationalDocument(String type) {
        super(type);
    }

}
