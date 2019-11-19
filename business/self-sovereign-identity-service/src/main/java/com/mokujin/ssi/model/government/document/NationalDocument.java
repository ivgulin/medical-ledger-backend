package com.mokujin.ssi.model.government.document;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class NationalDocument extends Document {

    public NationalDocument(String type) {
        super(type);
    }

}
