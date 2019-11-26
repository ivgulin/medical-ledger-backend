package com.mokujin.user.service;

import com.mokujin.user.model.User;
import com.mokujin.user.model.document.Document;
import com.mokujin.user.model.internal.DocumentDraft;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    User offerDicom(String publicKey, String privateKey, MultipartFile document, String patientNumber);

    User offerCredential(String publicKey, String privateKey, DocumentDraft documentDraft, String patientNumber);

    User accept(String publicKey, String privateKey, Document document, String nationalNumber, String connectionNumber);

    void decline(String nationalNumber, String connectionNumber);

    void askDocument(String publicKey, String privateKey, List<String> keywords, String connectionNumber);

    void presentDocument(String publicKey, String privateKey, Document document, String connectionNumber);


}
