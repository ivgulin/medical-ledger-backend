package com.mokujin.ssi.model.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mokujin.ssi.model.government.document.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder(toBuilder = true)
public class Credential {

    @JsonProperty("referent")
    private String id;

    @JsonProperty("attrs")
    private Document document;

    @JsonProperty("schema_id")
    private String schemaId;

    @JsonProperty("cred_def_id")
    private String schemaDefinitionId;

}
