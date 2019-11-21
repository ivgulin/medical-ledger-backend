package com.mokujin.user.controller;

import com.mokujin.user.model.document.Document;
import com.mokujin.user.model.presentation.Affirmation;
import com.mokujin.user.model.presentation.PresentationAttributes;
import com.mokujin.user.model.presentation.PresentationRequest;
import com.mokujin.user.model.presentation.Proof;
import com.mokujin.user.service.PresentationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.mokujin.user.model.User.Role;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/presentation")
public class PresentationController {

    private final PresentationService presentationService;

    @GetMapping("/attributes/{role}")
    public ResponseEntity<PresentationAttributes> getAttributes(@PathVariable Role role) {
        log.info("'getAttributes' is invoked: '{}'", role);

        PresentationAttributes presentationAttributes = presentationService.getPresentationAttributes(role);

        log.info("'getAttributes' returned value '{}'", presentationAttributes);
        return ResponseEntity.ok(presentationAttributes);
    }

    @PostMapping("/request/{connectionNumber}")
    public ResponseEntity request(@RequestBody PresentationRequest presentationRequest,
                                  @PathVariable String connectionNumber,
                                  @RequestHeader("Public-Key") String publicKey,
                                  @RequestHeader("Private-Key") String privateKey) {
        log.info("'request' is invoked: '{}, {}, {}, {}'", publicKey, privateKey, presentationRequest, connectionNumber);

        presentationService.requestPresentation(publicKey, privateKey, presentationRequest, connectionNumber);

        log.info("'request' has executed successfully.");
        return new ResponseEntity<>(OK);
    }

    @PostMapping("/present")
    public ResponseEntity present(@RequestBody Document document,
                                  @PathVariable String connectionNumber,
                                  @RequestHeader("Public-Key") String publicKey,
                                  @RequestHeader("Private-Key") String privateKey) {
        log.info("'present' is invoked: '{}, {}, {}'", publicKey, privateKey, document);

        presentationService.presentProof(publicKey, privateKey, document, connectionNumber);

        log.info("'present' has executed successfully.");
        return new ResponseEntity<>(OK);
    }

    @PostMapping("/verify")
    public ResponseEntity<Affirmation> verify(@RequestBody Proof proof,
                                              @PathVariable String nationalNumber,
                                              @PathVariable String connectionNumber) {
        log.info("'present' is invoked: '{}, {}, {}'", proof, nationalNumber, connectionNumber);

        Affirmation affirmation = presentationService.verifyProof(proof, nationalNumber, connectionNumber);

        log.info("'verify' returned value '{}'", affirmation);
        return ResponseEntity.ok(affirmation);
    }
}
