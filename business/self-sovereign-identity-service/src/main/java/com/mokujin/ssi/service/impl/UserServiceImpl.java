package com.mokujin.ssi.service.impl;

import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.user.response.User;
import com.mokujin.ssi.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {


    @Override
    public User convert(Identity identity) {
        return null;
    }
}
