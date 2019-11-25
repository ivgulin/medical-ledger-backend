package com.mokujin.user.service;

import org.hl7.fhir.dstu3.model.DomainResource;

public interface DocumentService {

    void send(String publicKey, String privateKey, DomainResource document, String patientNumber);

}
