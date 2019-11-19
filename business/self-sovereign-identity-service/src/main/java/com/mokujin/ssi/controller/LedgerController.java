package com.mokujin.ssi.controller;


import com.mokujin.ssi.model.user.request.UserCredentials;
import com.mokujin.ssi.model.user.request.UserRegistrationDetails;
import com.mokujin.ssi.model.user.response.User;
import com.mokujin.ssi.service.InvitationService;
import com.mokujin.ssi.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final RegistrationService registrationService;
    private final InvitationService invitationService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody UserRegistrationDetails details,
                                         @RequestParam("public") String publicKey,
                                         @RequestParam("private") String privateKey) {
        log.info("'register' invoked with params '{}, {}, {}'", details, publicKey, privateKey);

        User user = registrationService.register(details, publicKey, privateKey);

        log.info("'register' returned '{}'", user);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/connect")
    public ResponseEntity<User> connect(@RequestBody UserCredentials userCredentials,
                                        @RequestParam("public") String publicKey,
                                        @RequestParam("private") String privateKey) {
        log.info("'connect' invoked with params '{}, {}, {}'", userCredentials, publicKey, privateKey);

        User user = invitationService.connect(publicKey, privateKey, userCredentials);

        log.info("'register' returned '{}'", user);
        return ResponseEntity.ok(user);
    }
}
