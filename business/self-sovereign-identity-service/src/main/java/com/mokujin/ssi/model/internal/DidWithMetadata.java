package com.mokujin.ssi.model.internal;

import lombok.Data;

@Data
public class DidWithMetadata {

    private String did;

    private String verkey;

    private String tempVerkey;

    private Contact metadata;

}
