package com.mokujin.ssi.service.impl;

import com.mokujin.ssi.model.exception.extention.LedgerException;
import com.mokujin.ssi.model.exception.extention.ResourceNotFoundException;
import com.mokujin.ssi.model.document.Document;
import com.mokujin.ssi.model.government.document.NationalNumber;
import com.mokujin.ssi.model.government.document.NationalPassport;
import com.mokujin.ssi.model.internal.Contact;
import com.mokujin.ssi.model.internal.Credential;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Pseudonym;
import com.mokujin.ssi.model.user.response.User;
import com.mokujin.ssi.service.IdentityService;
import com.mokujin.ssi.service.WalletService;
import lombok.SneakyThrows;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private WalletService walletService;

    @Mock
    private IdentityService identityService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @Disabled
        // TODO: 23.11.19 fix it
    void convert_identityIsOk_userIsReturned() {

        String name = "John";
        String lastName = "Doe";
        long date = 1234356L;
        String place = "place";
        String image = "image";
        String sex = "male";
        String issuer = "gov";
        String number = "number";

        Credential passportCredential = Credential.builder()
                .document(new NationalPassport(number, name, lastName, name, date, place, image, sex, issuer, date))
                .build();

        Credential nationalNumberCredential = Credential.builder()
                .document(new NationalNumber(number, date, issuer))
                .build();

        Credential testCredential = Credential.builder()
                .document(new TestDocument(issuer))
                .build();

        Pseudonym pseudonym = Pseudonym.builder()
                .contact(Contact.builder()
                        .contactName(issuer)
                        .photo(image)
                        .build())
                .build();

        Identity identity = Identity.builder()
                .credentials(new ArrayList<>(Arrays.asList(passportCredential, testCredential, nationalNumberCredential)))
                .pseudonyms(Collections.singletonList(pseudonym))
                .build();

        User expected = User.builder()
                .lastName(lastName)
                .firstName(name)
                .fatherName(name)
                .photo(image)
                .nationalNumber(number)
                .contacts(Collections.singletonList(pseudonym.getContact()))
                .credentials(Collections.singletonList(testCredential))
                .nationalCredentials(Arrays.asList(passportCredential, nationalNumberCredential))
                .build();

        User result = userService.convert(identity);

        assertEquals(expected, result);
    }

    @Test
    void convert_NoPassportIsPresent_exceptionIsThrown() {

        Identity identity = Identity.builder()
                .credentials(Collections.emptyList())
                .build();

        assertThrows(ResourceNotFoundException.class, () -> userService.convert(identity));
    }

    @Test
    @SneakyThrows
    void get_exceptionOccursInsideTryBlock_walletIsClosedAndExceptionIsThrown() {

        String publicKey = "public";
        String privateKey = "private";

        Wallet wallet = Mockito.mock(Wallet.class);
        when(walletService.getOrCreateWallet(publicKey, privateKey)).thenReturn(wallet);

        when(identityService.findByWallet(wallet)).thenThrow(new LedgerException(INTERNAL_SERVER_ERROR, "test"));

        assertThrows(LedgerException.class, () -> userService.get(publicKey, privateKey));
        verify(wallet, times(1)).close();
    }

    @Test
    @SneakyThrows
    void get_validInputs_userIsReturned() {

        String publicKey = "public";
        String privateKey = "private";

        Wallet wallet = Mockito.mock(Wallet.class);
        when(walletService.getOrCreateWallet(publicKey, privateKey)).thenReturn(wallet);
        Identity identity = Identity.builder().build();
        when(identityService.findByWallet(wallet)).thenReturn(identity);

        User user = new User();
        userService = spy(userService);
        doReturn(user).when(userService).convert(identity);

        User result = userService.get(publicKey, privateKey);

        assertEquals(user, result);
        verify(wallet, times(1)).close();
    }

    class TestDocument extends Document {

        private String issuer;

        public TestDocument() {
            super("test");
        }

        public TestDocument(String issuer) {
            super("test");
            this.issuer = issuer;
        }
    }
}
