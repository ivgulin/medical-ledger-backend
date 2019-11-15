package com.mokujin.user.controller;

import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.User;
import com.mokujin.user.model.UserCredentials;
import com.mokujin.user.model.UserRegistrationDetails;
import com.mokujin.user.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/registration")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/create-wallet")
    public ResponseEntity<ProcessedUserCredentials> createWallet(@RequestBody @Valid UserCredentials userCredentials) {
        log.info("'createWallet' is invoked: '{}'", userCredentials);

        ProcessedUserCredentials processedUserCredentials = registrationService.createWallet(userCredentials);

        log.info("'createWallet' returned value '{}'", processedUserCredentials);
        return ResponseEntity.ok(processedUserCredentials);
    }

    @PostMapping("/create-user")
    public ResponseEntity<User> createUser(@RequestBody @Valid UserRegistrationDetails userDetails,
                                           @RequestHeader("Public-Key") String publicKey,
                                           @RequestHeader("Private-Key") String privateKey) {
        log.info("'createUser' is invoked: '{}, {}, {}'", userDetails, publicKey, privateKey);

        User user = registrationService.registerUser(userDetails, publicKey, privateKey);

        log.info("'createUser' returned value '{}'", user);
        return ResponseEntity.ok(user);
    }
}