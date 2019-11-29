package com.mokujin.oauth2.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

@FrameworkEndpoint
@RequiredArgsConstructor
public class RevokeTokenEndpoint {

    private static final String WEB_CLIENT_ID = "web_client_id";

    private final TokenStore tokenStore;

    @ResponseBody
    @DeleteMapping("/oauth/token")
    public void revokeToken(@RequestParam String username,
                            @RequestParam String refresh_token) {
        Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientIdAndUserName(WEB_CLIENT_ID, username);
        tokens.forEach(tokenStore::removeAccessToken);
        OAuth2RefreshToken oAuth2RefreshToken = tokenStore.readRefreshToken(refresh_token);
        tokenStore.removeRefreshToken(oAuth2RefreshToken);
    }
}
