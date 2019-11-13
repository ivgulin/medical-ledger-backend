package com.mokujin.ssi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mokujin.ssi.model.user.request.UserCredentials;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void deleteWallet_walletDoesNotExist_exceptionIsThrown() {

        String email = "test@test.com";
        String password = "test";
        UserCredentials credentials = new UserCredentials(email, password);

        when(walletService.doesWalletExist(anyString(), anyString())).thenReturn(false);

        deletionService.delete(credentials);

        Mockito.verify(walletService, times(0)).getOrCreateWallet(anyString(), anyString());
    }

    @Test
    @SneakyThrows
    void deleteWallet_walletExists_walletIsDeleted() {

        String email = "test@test.com";
        String password = "test";
        UserCredentials credentials = new UserCredentials(email, password);

        Wallet wallet = mock(Wallet.class);

        when(walletService.doesWalletExist(anyString(), anyString())).thenReturn(true);
        when(walletService.getOrCreateWallet(anyString(), anyString())).thenReturn(wallet);

        new MockUp<Wallet>() {
            @mockit.Mock
            public void deleteWallet(String config, String credentials) {
                log.debug("In mock.");
            }
        };

        deletionService.delete(credentials);
        Mockito.verify(walletService, times(1)).getOrCreateWallet(anyString(), anyString());
        Mockito.verify(wallet, times(1)).close();
    }


}