package com.mokujin.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Role {

    private Long id;

    private String role;
}
