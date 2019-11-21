package com.mokujin.ssi.service.impl;

import com.mokujin.ssi.model.government.KnownIdentity;
import com.mokujin.ssi.model.user.request.UserRegistrationDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private VerificationServiceImpl validationService;

    @Test
    void validateNewbie_detailsAreOk_identityIsReturned() {

        KnownIdentity knownIdentity = new KnownIdentity();

        when(restTemplate.postForObject(anyString(), any(), any())).thenReturn(knownIdentity);

        KnownIdentity result = validationService.verifyNewbie(new UserRegistrationDetails());

        assertEquals(knownIdentity, result);
    }
}