package com.mokujin.user.model.internal;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXISTING_PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = NAME, property = "type", include = EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ProcedureDraft.class, name = "Procedure")

})
public class DocumentDraft {

    private String type;

    public enum Type {
        Procedure
    }
}
