package com.mokujin.user.service;

import com.mokujin.user.model.User;
import com.mokujin.user.model.document.Document;
import com.mokujin.user.model.internal.DocumentDraft;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    User offerDicom(String publicKey, String privateKey, String image, String patientNumber);

    User offerCredential(String publicKey, String privateKey, DocumentDraft documentDraft, String patientNumber);

    User accept(String publicKey, String privateKey, Document document, String patientNumber, String doctorNumber);

    void decline(String patientNumber, String doctorNumber);

    User askDocument(String publicKey, String privateKey, List<String> keywords, String patientNumber);

    User shareDocument(String publicKey, String privateKey, Document document, String doctorNumber);

    void deleteNotification(String doctorNumber, String patientNumber);

}
