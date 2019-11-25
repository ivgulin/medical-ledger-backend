package com.mokujin.user.service;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {

    void sendDicom(String publicKey, String privateKey, MultipartFile document, String patientNumber);
    //void send(String publicKey, String privateKey, DomainResource document, String patientNumber);

}
