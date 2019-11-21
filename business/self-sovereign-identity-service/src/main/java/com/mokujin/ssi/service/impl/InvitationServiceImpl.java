package com.mokujin.ssi.service.impl;

import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.user.request.UserCredentials;
import com.mokujin.ssi.model.user.response.User;
import com.mokujin.ssi.service.IdentityService;
import com.mokujin.ssi.service.InvitationService;
import com.mokujin.ssi.service.UserService;
import com.mokujin.ssi.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.stereotype.Service;

import static org.hyperledger.indy.sdk.did.Did.createAndStoreMyDid;
import static org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private final WalletService walletService;
    private final IdentityService identityService;
    private final UserService userService;
    private final Pool pool;

    @Override
    public User connect(String publicKey, String privateKey, UserCredentials userCredentials) {

        try (Wallet doctorWallet = walletService.getOrCreateWallet(publicKey, privateKey);
             Wallet patientWallet = walletService.getOrCreateWallet(userCredentials.getPublicKey(),
                     userCredentials.getPrivateKey())) {

            Identity doctorIdentity = identityService.findByWallet(doctorWallet);
            Identity patientIdentity = identityService.findByWallet(patientWallet);

            CreateAndStoreMyDidResult patientPseudonym = createAndStoreMyDid(doctorWallet, "{}").get();
            CreateAndStoreMyDidResult doctorPseudonym = createAndStoreMyDid(patientWallet, "{}").get();

            identityService.establishUserConnection(pool, doctorIdentity, patientPseudonym, doctorPseudonym);

            identityService.exchangeContacts(doctorIdentity, patientIdentity, patientPseudonym, doctorPseudonym);

            return userService.convert(doctorIdentity);
        } catch (Exception e) {
            log.error("Exception was thrown: " + e);
            throw new LedgerException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
