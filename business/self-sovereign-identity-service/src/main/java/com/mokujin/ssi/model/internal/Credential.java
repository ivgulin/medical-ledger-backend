package com.mokujin.ssi.model.internal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.mokujin.ssi.model.document.Document;
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

    @JsonAlias({"referent", "id"})
    private String id;

    @JsonAlias({"attrs", "document"})
    private Document document;

    @JsonAlias({"schema_id", "schemaId"})
    private String schemaId;

    @JsonAlias({"cred_def_id", "schemaDefinitionId"})
    private String schemaDefinitionId;

}
