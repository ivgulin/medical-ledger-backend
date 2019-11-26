package com.mokujin.ssi.controller;

import com.mokujin.ssi.model.document.Document;
import com.mokujin.ssi.model.verification.Affirmation;
import com.mokujin.ssi.model.verification.Proof;
import com.mokujin.ssi.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping("/present")
    public ResponseEntity<Proof> presentProof(@RequestBody Document document,
                                              @RequestParam("public") String publicKey,
                                              @RequestParam("private") String privateKey) {
        log.info("'presentProof' invoked with params '{}, {}, {}'", publicKey, privateKey, document);

        Proof proof = verificationService.presentProof(publicKey, privateKey, document);

        log.info("'presentProof' returned '{}'", proof);
        return ResponseEntity.ok(proof);
    }

    @PostMapping("/verify")
    public ResponseEntity<Affirmation> verifyProof(@RequestBody Proof proof) {
        log.info("'verifyProof' invoked with params '{}'", proof);

        Affirmation affirmation = verificationService.verifyProof(proof);

        log.info("'verifyProof' returned '{}'", proof);
        return ResponseEntity.ok(affirmation);
    }


}
