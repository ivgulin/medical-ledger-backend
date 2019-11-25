package com.mokujin.user.controller;

import com.mokujin.user.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/send/dicom/{patientNumber}")
    public ResponseEntity sendDicom(@PathVariable String patientNumber,
                               @RequestParam("file") MultipartFile file,
                               @RequestHeader("Public-Key") String publicKey,
                               @RequestHeader("Private-Key") String privateKey) {
        log.info("'sendDicom' invoked with params '{}, {}, {}, {}'", patientNumber, file, publicKey, privateKey);

        documentService.sendDicom(publicKey, privateKey, file, patientNumber);

        log.info("'sendDicom' has executed successfully.");
        return new ResponseEntity(OK);
    }

    /*    @PostMapping("/send/procedure/{patientNumber}")
        public ResponseEntity send(@PathVariable String patientNumber,
                                   @RequestBody Procedure document,
                                   @RequestHeader("Public-Key") String publicKey,
                                   @RequestHeader("Private-Key") String privateKey) {
            log.info("'send' invoked with params '{}, {}, {}, {}'", patientNumber, document, publicKey, privateKey);

            documentService.send(publicKey, privateKey, document, patientNumber);

            log.info("'send' has executed successfully.");
            return new ResponseEntity(OK);
        }*/
}
