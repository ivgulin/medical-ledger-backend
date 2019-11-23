package com.mokujin.ssi.service.impl;

import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.user.request.UserCredentials;
import com.mokujin.ssi.model.user.response.User;
import com.mokujin.ssi.service.IdentityService;
import com.mokujin.ssi.service.UserService;
import com.mokujin.ssi.service.WalletService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import mockit.MockUp;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@ExtendWith(MockitoExtension.class)
class InvitationServiceImplTest {

    @Mock
    private WalletService walletService;

    @Mock
    private IdentityService identityService;

    @Mock
    private UserService userService;

    @Mock
    private Pool pool;

    @InjectMocks
    private InvitationServiceImpl invitationService;

    @Test
    @SneakyThrows
    void connect_exceptionOccursInsideTryBlock_walletIsClosedAndExceptionIsThrown() {

        String publicKey = "public";
        String privateKey = "private";
        UserCredentials userCredentials = new UserCredentials(publicKey, privateKey);

        Wallet wallet = Mockito.mock(Wallet.class);
        when(walletService.getOrCreateWallet(publicKey, privateKey)).thenReturn(wallet);

        doThrow(new LedgerException(INTERNAL_SERVER_ERROR, "Test exception"))
                .when(identityService).findByWallet(wallet);

        assertThrows(LedgerException.class, () -> invitationService.connect(publicKey, privateKey, userCredentials));
        verify(wallet, times(2)).close();
    }

    @Test
    @SneakyThrows
    void connect_validInputs_userIsReturned() {

        String publicKey = "public";
        String privateKey = "private";
        UserCredentials userCredentials = new UserCredentials(publicKey, privateKey);

        Wallet userWallet = Mockito.mock(Wallet.class);
        when(walletService.getOrCreateWallet(publicKey, privateKey)).thenReturn(userWallet);

        Identity identity = Identity.builder().build();
        when(identityService.findByWallet(userWallet)).thenReturn(identity);

        CreateAndStoreMyDidResult pseudonym = mock(CreateAndStoreMyDidResult.class);
        new MockUp<Did>() {
            @mockit.Mock
            public CompletableFuture<CreateAndStoreMyDidResult> createAndStoreMyDid(Wallet wallet, String didJson) {
                assertEquals(userWallet, wallet);

                CompletableFuture<CreateAndStoreMyDidResult> future = new CompletableFuture<>();
                future.complete(pseudonym);
                return future;
            }
        };

        User user = User.builder().build();
        when(userService.convert(identity)).thenReturn(user);

        User result = invitationService.connect(publicKey, privateKey, userCredentials);

        assertEquals(user, result);
        verify(userWallet, times(2)).close();
    }
}