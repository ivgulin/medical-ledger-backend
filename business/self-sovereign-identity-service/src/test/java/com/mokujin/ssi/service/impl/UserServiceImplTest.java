package com.mokujin.ssi.service.impl;

import com.mokujin.ssi.model.exception.extention.ResourceNotFoundException;
import com.mokujin.ssi.model.government.document.Document;
import com.mokujin.ssi.model.government.document.impl.NationalPassport;
import com.mokujin.ssi.model.internal.Contact;
import com.mokujin.ssi.model.internal.Credential;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Pseudonym;
import com.mokujin.ssi.model.user.response.User;
import com.mokujin.ssi.service.UserService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceImplTest {

    private UserService userService = new UserServiceImpl();

    @Test
    void convert_identityIsOk_userIsReturned() {

        String name = "John";
        String lastName = "Doe";
        long date = 1234356L;
        String place = "place";
        String image = "image";
        String sex = "male";
        String issuer = "gov";

        Credential passportCredential = Credential.builder()
                .document(new NationalPassport(name, lastName, name, date, place, image, sex, issuer, date))
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
                .credentials(new ArrayList<>(Arrays.asList(passportCredential, testCredential)))
                .pseudonyms(Collections.singletonList(pseudonym))
                .build();

        User expected = User.builder()
                .lastName(lastName)
                .firstName(name)
                .fatherName(name)
                .photo(image)
                .contacts(Collections.singletonList(pseudonym.getContact()))
                .credentials(Collections.singletonList(testCredential))
                .nationalCredentials(Collections.singletonList(passportCredential))
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