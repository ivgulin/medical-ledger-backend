package com.mokujin.oauth2.config;

import com.mokujin.oauth2.exception.CustomOauthException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableAuthorizationServer
@RequiredArgsConstructor
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private static final String WEB_CLIENT_ID = "web_client_id";
    private static final String WEB_CLIENT_SECRET = "web_client_secret";
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final TokenStore tokenStore;
    private final UserDetailsService userDetailsService;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()");
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
                .inMemory()
                .withClient(WEB_CLIENT_ID)
                .authorizedGrantTypes("password", "authorization_code", "refresh_token")
                .scopes("read", "write")
                .secret(passwordEncoder.encode(WEB_CLIENT_SECRET))
                .authorities("web-client")
                .accessTokenValiditySeconds(3600)
                .refreshTokenValiditySeconds(5000);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .tokenStore(tokenStore)
                .authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService)
                .exceptionTranslator(this::handleException);
    }

    public ResponseEntity<OAuth2Exception> handleException(Exception e) {
        return ResponseEntity
                .status(((OAuth2Exception) e).getHttpErrorCode())
                .body(new CustomOauthException(e.getMessage()));
    }
}
