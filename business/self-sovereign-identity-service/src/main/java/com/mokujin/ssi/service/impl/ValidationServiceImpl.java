package com.mokujin.ssi.service.impl;

import com.mokujin.ssi.model.government.KnownIdentity;
import com.mokujin.ssi.model.user.request.UserRegistrationDetails;
import com.mokujin.ssi.service.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private final RestTemplate restTemplate;

    @Override
    public KnownIdentity validateNewbie(UserRegistrationDetails details) {

        log.info("'validateNewbie' invoked with params'{}'", details);

        KnownIdentity knownIdentity = restTemplate
                .postForObject("http://fake-government-service/identity/issue-credentials",
                        details, KnownIdentity.class);

        log.info("'validateNewbie' returned value '{}'", knownIdentity);

        return knownIdentity;

    }
}
