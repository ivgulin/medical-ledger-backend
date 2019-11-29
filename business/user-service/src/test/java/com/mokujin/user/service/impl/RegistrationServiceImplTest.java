package com.mokujin.user.service.impl;

import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.User;
import com.mokujin.user.model.UserCredentials;
import com.mokujin.user.model.UserRegistrationDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @Test
    void createWallet_credentialsAreOk_processedCredentialsAreReturned() {

        String email = "test@test.com";
        String password = "test";
        UserCredentials credentials = new UserCredentials(email, password);

        ProcessedUserCredentials expected = ProcessedUserCredentials.builder()
                .publicKey(email)
                .privateKey(password)
                .build();
        ProcessedUserCredentials result = registrationService.createWallet(credentials);

        assertEquals(expected, result);
    }

    @Test
    void registerUser_credentialsAreOk_userIsReturned() {
        User user = new User();
        user.setFirstName("name");

        when(restTemplate.postForObject(anyString(), any(), any())).thenReturn(user);

        User result = registrationService.registerUser(new UserRegistrationDetails(), "test", "test");

        assertEquals(user, result);
    }

}