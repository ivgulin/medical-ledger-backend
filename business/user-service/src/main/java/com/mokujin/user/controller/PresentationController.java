package com.mokujin.user.controller;

import com.mokujin.user.model.document.NationalDocument;
import com.mokujin.user.model.presentation.PresentationAttributes;
import com.mokujin.user.model.presentation.PresentationRequest;
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
    public ResponseEntity<PresentationAttributes> present(@RequestBody NationalDocument nationalDocument,
                                                          @RequestHeader("Public-Key") String publicKey,
                                                          @RequestHeader("Private-Key") String privateKey) {
        log.info("'present' is invoked: '{}, {}, {}'", publicKey, privateKey, nationalDocument);

        presentationService.presentProof(publicKey, privateKey, nationalDocument);

        log.info("'present' has executed successfully.");
        return new ResponseEntity<>(OK);
    }
}
