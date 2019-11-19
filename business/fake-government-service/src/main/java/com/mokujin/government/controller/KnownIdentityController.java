package com.mokujin.government.controller;

import com.mokujin.government.model.dto.KnownIdentityDTO;
import com.mokujin.government.model.dto.Person;
import com.mokujin.government.model.entity.KnownIdentity;
import com.mokujin.government.service.KnownIdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/identity")
public class KnownIdentityController {

    private final KnownIdentityService knownIdentityService;

    @PostMapping("/save")
    public ResponseEntity<KnownIdentity> save(@RequestBody @Valid KnownIdentity knownIdentity) {
        log.info("'save' is invoked: '{}'", knownIdentity);

        KnownIdentity savedKnownIdentity = knownIdentityService.save(knownIdentity);

        log.info("'save' returned value '{}'", savedKnownIdentity);
        return ResponseEntity.ok(savedKnownIdentity);
    }

    @PostMapping("/upload-photo/{identityId}")
    public ResponseEntity<KnownIdentity> uploadPhoto(@PathVariable Integer identityId,
                                                     @RequestParam("file") MultipartFile file) {
        log.info("'uploadPhoto' is invoked: '{}'", identityId);

        KnownIdentity updatedKnownIdentity = knownIdentityService.uploadPhoto(identityId, file);

        log.info("'uploadPhoto' returned value '{}'", updatedKnownIdentity);
        return ResponseEntity.ok(updatedKnownIdentity);
    }

    @GetMapping("/{identityId}")
    public ResponseEntity<KnownIdentity> get(@PathVariable Integer identityId) {
        log.info("'get' is invoked: '{}'", identityId);

        KnownIdentity knownIdentity = knownIdentityService.get(identityId);

        log.info("'get' returned value '{}'", knownIdentity);
        return ResponseEntity.ok(knownIdentity);
    }

    @PostMapping("/issue-credentials")
    public ResponseEntity<KnownIdentityDTO> issueCredentials(@RequestBody @Valid Person person) {
        log.info("'issueCredentials' is invoked: '{}'", person);

        KnownIdentityDTO knownIdentity = knownIdentityService.getWithImage(person);

        log.info("'issueCredentials' returned value '{}'", knownIdentity);
        return ResponseEntity.ok(knownIdentity);
    }
}
