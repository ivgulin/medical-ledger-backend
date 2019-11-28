package com.mokujin.zuul.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;

import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Value("${auth.server.url}")
    private String authUrl;

    @Value("${auth.server.clientId}")
    private String clientId;

    @Value("${auth.server.clientSecret}")
    private String clientSecret;

    // TODO: 27.11.19 fix it
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/docs/**").permitAll()
                /*.antMatchers("/user/invitation/invite-back", "/user/document/accept", "/user/document/decline",
                        "/user/document/present/**", "/user/health/save", "/user/health/share/**").hasRole(Role.PATIENT.name())
                .antMatchers("/user/invitation/accept", "/user/invitation/decline", "/user/document/offer/**",
                        "/user/document/ask/**", "/user/document/delete/**", "/user/health/delete/**").hasRole(Role.DOCTOR.name())*/
                .antMatchers("/auth/**", "/gov/**").denyAll()
                .antMatchers(POST, "/user/registration/**").denyAll()
                .anyRequest().authenticated();
    }

    @Bean
    public RemoteTokenServices tokenService() {
        RemoteTokenServices tokenService = new RemoteTokenServices();
        tokenService.setCheckTokenEndpointUrl(authUrl);
        tokenService.setClientId(clientId);
        tokenService.setClientSecret(clientSecret);
        return tokenService;
    }
}
