package com.mokujin.auth.model;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable{

    private Integer userId;

    private String username;

    private String password;

    private String email;

    private Set<Role> roles = new HashSet<>();
}
