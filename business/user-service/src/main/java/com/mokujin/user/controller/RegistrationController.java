package com.mokujin.user.controller;

import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.User;
import com.mokujin.user.model.UserCredentials;
import com.mokujin.user.model.UserRegistrationDetails;
import com.mokujin.user.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/registration")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/create-wallet")
    public ResponseEntity<ProcessedUserCredentials> createWallet(@RequestBody UserCredentials userCredentials) {
        log.info("'createWallet' is invoked: '{}'", userCredentials);

        ProcessedUserCredentials processedUserCredentials = registrationService.createWallet(userCredentials);

        log.info("'createWallet' returned value '{}'", processedUserCredentials);
        return ResponseEntity.ok(processedUserCredentials);
    }

    @PostMapping("/create-user")
    public ResponseEntity<User> createUser(@RequestBody UserRegistrationDetails userDetails) {
        log.info("'createUser' is invoked: '{}'", userDetails);

        User user = registrationService.registerUser(userDetails);

        log.info("'createUser' returned value '{}'", user);
        return ResponseEntity.ok(user);
    }
}