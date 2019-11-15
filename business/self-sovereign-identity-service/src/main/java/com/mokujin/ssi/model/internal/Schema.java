package com.mokujin.ssi.model.internal;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder(toBuilder = true)
public class Schema {

    private String schemaId;

    private String schema;

    private String schemaDefinitionId;

    private String schemaDefinition;

}
