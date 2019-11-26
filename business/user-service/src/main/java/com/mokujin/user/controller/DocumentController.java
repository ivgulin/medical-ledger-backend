package com.mokujin.user.controller;

import com.mokujin.user.model.User;
import com.mokujin.user.model.internal.DocumentDraft;
import com.mokujin.user.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/offer/dicom/{patientNumber}")
    public ResponseEntity<User> offerDicom(@PathVariable String patientNumber,
                                           @RequestParam("file") MultipartFile file,
                                           @RequestHeader("Public-Key") String publicKey,
                                           @RequestHeader("Private-Key") String privateKey) {
        log.info("'offerDicom' invoked with params '{}, {}, {}, {}'", patientNumber, file, publicKey, privateKey);

        User user = documentService.offerDicom(publicKey, privateKey, file, patientNumber);

        log.info("'offerDicom' returned '{}'", user);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/offer/{patientNumber}")
    public ResponseEntity offer(@PathVariable String patientNumber,
                                @RequestBody DocumentDraft documentDraft,
                                @RequestHeader("Public-Key") String publicKey,
                                @RequestHeader("Private-Key") String privateKey) {
        log.info("'send' invoked with params '{}, {}, {}, {}'", patientNumber, documentDraft, publicKey, privateKey);

        User user = documentService.offerCredential(publicKey, privateKey, documentDraft, patientNumber);

        log.info("'offer' returned '{}'", user);
        return ResponseEntity.ok(user);
    }
}
