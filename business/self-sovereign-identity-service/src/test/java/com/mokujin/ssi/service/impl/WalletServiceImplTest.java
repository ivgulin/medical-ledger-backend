package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokujin.ssi.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import mockit.Mock;
import mockit.MockUp;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@Slf4j
class WalletServiceImplTest {

    private WalletService walletService = new WalletServiceImpl(new ObjectMapper());

    @Test
    void getOrCreateWallet_validInputs_walletIsReturned() {

        Wallet wallet = mock(Wallet.class);

        new MockUp<Wallet>() {
            @Mock
            public CompletableFuture<Wallet> openWallet(String config, String credentials) {
                CompletableFuture<Wallet> future = new CompletableFuture<>();
                future.complete(wallet);
                return future;
            }
        };

        Wallet result = walletService.getOrCreateWallet("test", "test");
        assertEquals(wallet, result);
    }

    @Test
    void getOrCreateWallet_walletDoesNotExist_newWalletIsReturned() {

        IndyException indyException = mock(IndyException.class);

        new MockUp<Wallet>() {
            @Mock
            public CompletableFuture<Wallet> openWallet(String config, String credentials) throws IndyException {
                throw indyException;
            }

            @Mock
            public CompletableFuture<Void> createWallet(String config, String credentials) {
                return CompletableFuture.runAsync(() -> log.debug("In mock."));
            }
        };

        assertThrows(IndyException.class, () -> walletService.getOrCreateWallet("test", "test"));
    }

    @ParameterizedTest
    @CsvSource({"valid,valid,true", "invalid,valid,false"})
    void doesWalletExist_validInputs_walletIsReturned(String publicKey, String privateKey, boolean expected) {

        Wallet wallet = mock(Wallet.class);
        new MockUp<Wallet>() {
            @Mock
            public CompletableFuture<Wallet> openWallet(String config, String credentials) throws Exception {
                System.out.println("public = " + config);

                if (config.equals("{\"id\":\"invalid\"}")) throw new Exception();
                CompletableFuture<Wallet> future = new CompletableFuture<>();
                future.complete(wallet);
                return future;
            }
        };

        boolean result = walletService.doesWalletExist(publicKey, privateKey);
        assertEquals(expected, result);
    }
}