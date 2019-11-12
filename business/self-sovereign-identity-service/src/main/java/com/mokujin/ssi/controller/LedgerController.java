package com.mokujin.ssi.controller;


import com.mokujin.ssi.model.user.request.UserCredentials;
import com.mokujin.ssi.model.user.request.UserRegistrationDetails;
import com.mokujin.ssi.model.user.response.User;
import com.mokujin.ssi.service.DeletionService;
import com.mokujin.ssi.service.RegistrationService;
import com.mokujin.ssi.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final WalletService walletService;
    private final RegistrationService registrationService;
    private final DeletionService deletionService;

    @SneakyThrows
    @PostMapping("/get-wallet")
    public ResponseEntity getWallet(@RequestBody UserCredentials credentials) {
        log.info("'register' invoked with params'{}'", credentials);

        Wallet wallet = walletService.getOrCreateWallet(credentials.getPublicKey(), credentials.getPrivateKey());
        wallet.close();

        log.info("getWallet is executed successfully.");
        return new ResponseEntity(OK);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody UserRegistrationDetails details,
                                         @RequestParam("public") String publicKey,
                                         @RequestParam("private") String privateKey) {
        log.info("'register' invoked with params'{}'", details);

        User user = registrationService.register(details, publicKey, privateKey);

        log.info("register is executed successfully.");
        return ResponseEntity.ok(user);
    }

    @PostMapping("/delete")
    public ResponseEntity delete(@RequestBody UserCredentials credentials) {
        log.info("'delete' invoked with params'{}'", credentials);

        deletionService.delete(credentials);

        log.info("delete is executed successfully.");
        return new ResponseEntity(OK);
    }
}
