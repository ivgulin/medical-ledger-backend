package com.mokujin.user.service;

import com.mokujin.user.model.User;
import com.mokujin.user.model.internal.DocumentDraft;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {

    User offerDicom(String publicKey, String privateKey, MultipartFile document, String patientNumber);

    User offerCredential(String publicKey, String privateKey, DocumentDraft documentDraft, String patientNumber);

}
