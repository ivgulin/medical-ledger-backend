package com.mokujin.ssi.model.user.response;

import com.mokujin.ssi.model.internal.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Auth {

    private boolean exists;

    private Role role;

}
