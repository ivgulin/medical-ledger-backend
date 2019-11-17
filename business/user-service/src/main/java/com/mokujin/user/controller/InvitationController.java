package com.mokujin.user.controller;

import com.mokujin.user.model.User;
import com.mokujin.user.service.InvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("/invitation")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @GetMapping("/invite-back")
    public ResponseEntity inviteBack(@RequestParam String invitorNumber,
                                     @RequestHeader("Public-Key") String publicKey,
                                     @RequestHeader("Private-Key") String privateKey) {
        log.info("'inviteBack' invoked with params '{}, {}, {}'", publicKey, privateKey, invitorNumber);

        User user = invitationService.inviteBack(publicKey, privateKey, invitorNumber);

        log.info("'inviteBack' returned '{}'", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/accept")
    public ResponseEntity<User> accept(@RequestParam String nationalNumber,
                                       @RequestHeader("Public-Key") String publicKey,
                                       @RequestHeader("Private-Key") String privateKey) {
        log.info("'accept' invoked with params '{}, {}, {}'", publicKey, privateKey, nationalNumber);

        User user = invitationService.accept(publicKey, privateKey, nationalNumber);

        log.info("'accept' returned '{}'", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/decline")
    public ResponseEntity decline(@RequestParam String nationalNumber) {
        log.info("'decline' invoked with params '{}'", nationalNumber);

        invitationService.decline(nationalNumber);

        log.info("'decline' has executed successfully.");
        return new ResponseEntity(OK);
    }

}
