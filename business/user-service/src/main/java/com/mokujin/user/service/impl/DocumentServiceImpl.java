package com.mokujin.user.service.impl;

import com.mokujin.user.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final RestTemplate restTemplate;

    @Override
    public void send(String publicKey, String privateKey, DomainResource document, String patientNumber) {



    }
}
