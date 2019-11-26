package com.mokujin.ssi.controller;


import com.mokujin.ssi.model.user.request.OfferRequest;
import com.mokujin.ssi.model.user.response.User;
import com.mokujin.ssi.service.CredentialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
