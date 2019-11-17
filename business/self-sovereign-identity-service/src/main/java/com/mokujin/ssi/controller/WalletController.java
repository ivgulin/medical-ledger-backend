package com.mokujin.ssi.controller;

import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.user.request.UserCredentials;
import com.mokujin.ssi.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @SneakyThrows
    @PostMapping("/create")
    public ResponseEntity createWallet(@RequestBody UserCredentials credentials) {
        log.info("'createWallet' invoked with params '{}'", credentials);

        try (Wallet wallet = walletService.getOrCreateWallet(credentials.getPublicKey(), credentials.getPrivateKey());) {
            log.info("'createWallet' is executed successfully.");
            return new ResponseEntity(OK);
        }catch (Exception e) {
            log.error("Exception was thrown: " + e);
            throw new LedgerException(INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }

    @SneakyThrows
    @PostMapping("/check")
    public ResponseEntity<Boolean> checkWallet(@RequestBody UserCredentials credentials) {
        log.info("'checkWallet' invoked with params '{}'", credentials);

        boolean doesWalletExist = walletService.doesWalletExist(credentials.getPublicKey(), credentials.getPrivateKey());

        log.info("'checkWallet' returns = '{}'", doesWalletExist);
        return ResponseEntity.ok(doesWalletExist);
    }

}
