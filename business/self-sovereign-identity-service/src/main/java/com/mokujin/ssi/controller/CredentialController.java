package com.mokujin.ssi.controller;


import com.mokujin.ssi.model.user.request.OfferRequest;
import com.mokujin.ssi.model.user.response.User;
import com.mokujin.ssi.service.CredentialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("/credential")
@RequiredArgsConstructor
public class CredentialController {

    private final CredentialService credentialService;

    @PostMapping("/add")
    public ResponseEntity<User> add(@RequestBody OfferRequest offerRequest,
                                    @RequestParam("public") String publicKey,
                                    @RequestParam("private") String privateKey) {
        log.info("'add' invoked with params '{}, {}, {}'", publicKey, privateKey, offerRequest);

        User user = credentialService.addCredential(publicKey, privateKey, offerRequest);

        log.info("'add' returned '{}'", user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/delete/{credentialId}")
    public ResponseEntity delete(@PathVariable String credentialId,
                                 @RequestParam("public") String publicKey,
                                 @RequestParam("private") String privateKey) {

        log.info("'delete' invoked with params '{}, {}, {}'", publicKey, privateKey, credentialId);

        credentialService.deleteCredential(publicKey, privateKey, credentialId);

        log.info("'add' is executed successfully");
        return new ResponseEntity(OK);
    }
}
