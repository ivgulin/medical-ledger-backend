package com.mokujin.ssi.service.impl;

import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.exception.extention.ResourceNotFoundException;
import com.mokujin.ssi.model.government.document.NationalDocument;
import com.mokujin.ssi.model.government.document.impl.NationalNumber;
import com.mokujin.ssi.model.government.document.impl.NationalPassport;
import com.mokujin.ssi.model.internal.Credential;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Pseudonym;
import com.mokujin.ssi.model.user.response.User;
import com.mokujin.ssi.service.IdentityService;
import com.mokujin.ssi.service.UserService;
import com.mokujin.ssi.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final WalletService walletService;
    private final IdentityService identityService;

    @Override
    @SneakyThrows
    public User convert(Identity identity) {

        List<Credential> credentials = identity.getCredentials();

        List<Credential> nationalCredentials = credentials.stream()
                .filter(c -> c.getDocument() instanceof NationalDocument)
                .collect(Collectors.toList());

        NationalPassport passport = (NationalPassport) nationalCredentials.stream()
                .filter(c -> NationalPassport.class.equals(c.getDocument().getClass()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No passport has been found."))
                .getDocument();

        NationalNumber nationalNumber = (NationalNumber) nationalCredentials.stream()
                .filter(c -> NationalNumber.class.equals(c.getDocument().getClass()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No national number has been found."))
                .getDocument();

        credentials.removeAll(nationalCredentials);

        return User.builder()
                .role(identity.getRole())
                .lastName(passport.getLastName())
                .firstName(passport.getFirstName())
                .fatherName(passport.getFatherName())
                .nationalNumber(nationalNumber.getNumber())
                .photo(passport.getImage())
                .contacts(identity.getPseudonyms().stream().map(Pseudonym::getContact).collect(Collectors.toList()))
                .credentials(credentials)
                .nationalCredentials(nationalCredentials)
                .build();
    }

    @Override
    public User get(String publicKey, String privateKey) {

        try (Wallet userWallet = walletService.getOrCreateWallet(publicKey, privateKey);){
            Identity userIdentity = identityService.findByWallet(userWallet);
            return this.convert(userIdentity);
        }catch (Exception e) {
            log.error("Exception was thrown: " + e);
            throw new LedgerException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
