package com.mokujin.ssi.controller;


import com.mokujin.ssi.model.record.HealthRecord;
import com.mokujin.ssi.service.HealthDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthDataController {

    private final HealthDataService healthDataService;

    @PostMapping("/save")
    public ResponseEntity<List<HealthRecord>> save(@RequestBody HealthRecord record,
                                                   @RequestParam("public") String publicKey,
                                                   @RequestParam("private") String privateKey) {
        log.info("'save' invoked with params '{}, {}, {}'", record, publicKey, privateKey);

        List<HealthRecord> records = healthDataService.save(publicKey, privateKey, record);

        log.info("'save' returned '{}'", records);
        return ResponseEntity.ok(records);
    }

}
