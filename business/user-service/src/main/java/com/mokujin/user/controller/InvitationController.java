package com.mokujin.user.controller;

import com.mokujin.user.model.Contact;
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

    @PostMapping("/invite-back")
    public ResponseEntity<User> inviteBack(@RequestBody Contact doctor,
                                           @RequestHeader("Public-Key") String publicKey,
                                           @RequestHeader("Private-Key") String privateKey) {
        log.info("'inviteBack' invoked with params '{}, {}, {}'", publicKey, privateKey, doctor);

        User user = invitationService.inviteBack(publicKey, privateKey, doctor);

        log.info("'inviteBack' returned '{}'", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/accept")
    public ResponseEntity<User> accept(@RequestParam String doctorNumber,
                                       @RequestParam String patientNumber,
                                       @RequestHeader("Public-Key") String publicKey,
                                       @RequestHeader("Private-Key") String privateKey) {
        log.info("'accept' invoked with params '{}, {}, {}, {}'", publicKey, privateKey, doctorNumber, patientNumber);

        User user = invitationService.accept(publicKey, privateKey, doctorNumber, patientNumber);

        log.info("'accept' returned '{}'", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/decline")
    public ResponseEntity decline(@RequestParam String doctorNumber, @RequestParam String patientNumber) {
        log.info("'decline' invoked with params '{}, {}'", doctorNumber, patientNumber);

        invitationService.decline(doctorNumber, patientNumber);

        log.info("'decline' has executed successfully.");
        return new ResponseEntity(OK);
    }

}
