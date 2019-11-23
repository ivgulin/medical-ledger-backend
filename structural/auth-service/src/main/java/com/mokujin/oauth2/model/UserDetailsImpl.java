package com.mokujin.oauth2.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class UserDetailsImpl implements UserDetails {

    private String userName;
    private String password;
    private List<GrantedAuthority> roles;

    public UserDetailsImpl(User user) {
        log.info("in user detail constructor with params'{}'", user);
        this.userName = user.getUsername();
        this.password = user.getPassword();
        this.roles = user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.name())).collect(Collectors.toList());
        log.info("user details created");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
