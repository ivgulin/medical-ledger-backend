package com.mokujin.auth.service;

import com.mokujin.auth.model.User;
import com.mokujin.auth.model.UserDetailsImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if ("test".equals(username)) {
            User user = new User(1, "test",
                    "$2a$10$APikXN.LGwEBvb4KkHN0wegMpgLbDKNI2SoJ6tUu.4ZCAwMPf2K2u",
                    "test@test.com", Collections.emptySet());
            return new UserDetailsImpl(user) {
            };
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}