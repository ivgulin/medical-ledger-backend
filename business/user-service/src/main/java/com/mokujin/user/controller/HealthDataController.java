package com.mokujin.user.controller;

import com.mokujin.user.model.User;
import com.mokujin.user.model.record.HealthRecord;
import com.mokujin.user.service.HealthDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthDataController {

    private final HealthDataService healthDataService;

    @PostMapping("/save")
    public ResponseEntity<List<HealthRecord>> save(@RequestBody HealthRecord record,
                                                   @RequestHeader("Public-Key") String publicKey,
                                                   @RequestHeader("Private-Key") String privateKey) {
        log.info("'save' invoked with params '{}, {}, {}'", record, publicKey, privateKey);

        List<HealthRecord> records = healthDataService.save(publicKey, privateKey, record);

        log.info("'save' returned '{}'", records);
        return ResponseEntity.ok(records);
    }

    @PostMapping("/share/{doctorNumber}")
    public ResponseEntity<User> share(@PathVariable String doctorNumber,
                                     @RequestBody HealthRecord record,
                                     @RequestHeader("Public-Key") String publicKey,
                                     @RequestHeader("Private-Key") String privateKey) {
        log.info("'share' invoked with params '{}, {}, {}, {}'", doctorNumber, record, publicKey, privateKey);

        User user = healthDataService.share(publicKey, privateKey, record, doctorNumber);

        log.info("'share' returned '{}'", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/delete/notification")
    public ResponseEntity deleteNotification(@RequestParam String patientNumber,
                                             @RequestParam String doctorNumber) {
        log.info("'deleteNotification' invoked with params '{}, {}'", patientNumber, doctorNumber);

        healthDataService.deleteNotification(patientNumber, doctorNumber);

        log.info("'deleteNotification' has executed successfully.");
        return new ResponseEntity(OK);
    }

}
