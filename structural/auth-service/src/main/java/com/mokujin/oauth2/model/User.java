package com.mokujin.oauth2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    private String username;

    private String password;

    private Set<Role> roles = new HashSet<>();
}
