package com.mokujin.ssi.model.government.document;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NationalDocument extends Document {

    public NationalDocument(String type) {
        super(type);
    }

}
