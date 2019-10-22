package com.mokujin.user.service.impl;

import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.User;
import com.mokujin.user.model.UserCredentials;
import com.mokujin.user.model.UserRegistrationDetails;
import com.mokujin.user.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final RestTemplate restTemplate;

    @Override
    public ProcessedUserCredentials createWallet(UserCredentials userCredentials) {

        throw new NotImplementedException();

        // TODO: 10/22/2019 encrypt password
        //String encryptedPassword = null;

        // TODO: 10/22/2019 complete request to newly created blockchain service
      /*  restTemplate.getForObject("http://test-service/test-integration", String.class);

        return ProcessedUserCredentials.builder()
                .publicKey(userCredentials.getEmail())
                .privateKey(encryptedPassword)
                .build();*/
    }

    @Override
    public User registerUser(UserRegistrationDetails userRegistrationDetails) {
        throw new NotImplementedException();
    }
}
