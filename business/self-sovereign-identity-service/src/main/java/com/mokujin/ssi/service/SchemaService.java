package com.mokujin.ssi.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Schema;
import org.hyperledger.indy.sdk.pool.Pool;

public interface SchemaService {

    Schema getSchema(Pool pool, Identity trustAnchor, String schemaName, String tag, ArrayNode attributes);
}
