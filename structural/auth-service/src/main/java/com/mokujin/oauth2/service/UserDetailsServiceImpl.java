package com.mokujin.oauth2.service;


import com.mokujin.oauth2.model.AuthResponse;
import com.mokujin.oauth2.model.User;
import com.mokujin.oauth2.model.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final RestTemplate restTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        String[] credentials = username.split(",");

        AuthResponse authResponse = restTemplate
                .getForObject("http://self-sovereign-identity-service/wallet/check" +
                        "?public=" + credentials[0] +
                        "&private=" + credentials[1], AuthResponse.class);

        if (authResponse.isExists()) {
            User user = new User(1, "test",
                    "$2a$10$APikXN.LGwEBvb4KkHN0wegMpgLbDKNI2SoJ6tUu.4ZCAwMPf2K2u",
                    "test@test.com", Collections.singleton(authResponse.getRole()));
            return new UserDetailsImpl(user);
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}