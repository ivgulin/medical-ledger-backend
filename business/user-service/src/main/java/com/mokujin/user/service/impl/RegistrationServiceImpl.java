package com.mokujin.user.service.impl;

import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.User;
import com.mokujin.user.model.UserCredentials;
import com.mokujin.user.model.UserRegistrationDetails;
import com.mokujin.user.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final RestTemplate restTemplate;

    @Override
    public ProcessedUserCredentials createWallet(UserCredentials userCredentials) {

        String encryptedPassword = bCryptPasswordEncoder
                .encode(userCredentials.getEmail() + userCredentials.getPassword());
        log.info("encryptedPassword =  '{}'", encryptedPassword);

        ProcessedUserCredentials processedUserCredentials = ProcessedUserCredentials.builder()
                .publicKey(userCredentials.getEmail())
                .privateKey(encryptedPassword)
                .build();

        restTemplate.postForLocation("http://self-sovereign-identity-service/wallet/create",
                processedUserCredentials);

        return processedUserCredentials;
    }

    @Override
    public User registerUser(UserRegistrationDetails userRegistrationDetails, String publicKey, String privateKey) {
        String url = "http://self-sovereign-identity-service/ledger/register?public="
                + publicKey + "&private=" + privateKey;
        return restTemplate.postForObject(url, userRegistrationDetails, User.class);
    }
}
