package com.mokujin.ssi.service;

import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.user.response.User;

public interface UserService {

    User convert(Identity identity);

}
