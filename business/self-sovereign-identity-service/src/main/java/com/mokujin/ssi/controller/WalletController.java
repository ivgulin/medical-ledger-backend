package com.mokujin.ssi.controller;

import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.user.request.UserCredentials;
import com.mokujin.ssi.model.user.response.Auth;
import com.mokujin.ssi.service.DeletionService;
import com.mokujin.ssi.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final DeletionService deletionService;

    @PostMapping("/create")
    public ResponseEntity createWallet(@RequestBody UserCredentials credentials) {
        log.info("'createWallet' invoked with params '{}'", credentials);

        try (Wallet wallet = walletService.getOrCreateWallet(credentials.getPublicKey(), credentials.getPrivateKey());) {
            log.info("'createWallet' is executed successfully.");
            return new ResponseEntity(OK);
        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            throw new LedgerException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/check")
    public ResponseEntity<Auth> checkWallet(@RequestParam("public") String publicKey,
                                            @RequestParam("private") String privateKey) {
        log.info("'checkWallet' invoked with params '{}, {}'", publicKey, privateKey);

        Auth auth = walletService.doesWalletExist(publicKey, privateKey);

        log.info("'checkWallet' returns = '{}'", auth);
        return ResponseEntity.ok(auth);
    }

    @DeleteMapping("/delete")
    public ResponseEntity delete(@RequestParam("public") String publicKey,
                                 @RequestParam("private") String privateKey) {
        log.info("'delete' invoked with params '{}, {}'", publicKey, privateKey);

        deletionService.delete(publicKey, privateKey);

        log.info("delete is executed successfully.");
        return new ResponseEntity(OK);
    }

}
