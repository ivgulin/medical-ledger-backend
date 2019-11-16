package com.mokujin.user.service;

import com.mokujin.user.model.User;

public interface UserService {

    User get(String publicKey, String privateKey);

}
