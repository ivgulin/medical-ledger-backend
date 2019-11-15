package com.mokujin.user.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class UserCredentials {

    @NonNull
    private String email;

    @NonNull
    private String password;

}
