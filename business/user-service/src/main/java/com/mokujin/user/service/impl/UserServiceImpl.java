package com.mokujin.user.service.impl;

import com.mokujin.user.model.User;
import com.mokujin.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final RestTemplate restTemplate;

    @Override
    public User get(String publicKey, String privateKey) {
        String url = "http://self-sovereign-identity-service/user/get?public="
                + publicKey + "&private=" + privateKey;
        return restTemplate.getForObject(url, User.class);
    }
}
