package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokujin.ssi.model.exception.extention.ResourceNotFoundException;
import com.mokujin.ssi.model.user.response.Auth;
import com.mokujin.ssi.service.DeletionService;
import com.mokujin.ssi.service.WalletService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import mockit.MockUp;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class DeletionServiceImplTest {

    @Mock
    private WalletService walletService;

    private DeletionService deletionService;

    @BeforeEach
    void setUp() {
        deletionService = new DeletionServiceImpl(new ObjectMapper(), walletService);
    }

    @Test
    @SneakyThrows
    void deleteWallet_walletDoesNotExist_exceptionIsThrown() {

        String email = "test@test.com";
        String password = "test";

        when(walletService.doesWalletExist(anyString(), anyString())).thenReturn(new Auth(false, null));

        assertThrows(ResourceNotFoundException.class, () -> deletionService.delete(email, password));

        verify(walletService, times(0)).getOrCreateWallet(anyString(), anyString());
    }

    @Test
    @SneakyThrows
    void deleteWallet_walletExists_walletIsDeleted() {

        String email = "test@test.com";
        String password = "test";

        when(walletService.doesWalletExist(anyString(), anyString())).thenReturn(new Auth(true, null));

        new MockUp<Wallet>() {
            @mockit.Mock
            public CompletableFuture<Void> deleteWallet(String config, String credentials) {


                assertTrue(config.contains(email));
                assertTrue(credentials.contains(password));
                return CompletableFuture.runAsync(() -> log.debug("In mock."));
            }
        };

        deletionService.delete(email, password);
    }


}