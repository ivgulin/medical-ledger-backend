package com.mokujin.ssi.service.impl;

import com.mokujin.ssi.model.exception.ResourceNotFoundException;
import com.mokujin.ssi.model.government.NationalDocument;
import com.mokujin.ssi.model.government.NationalPassport;
import com.mokujin.ssi.model.internal.Credential;
import com.mokujin.ssi.model.internal.Identity;
import com.mokujin.ssi.model.internal.Pseudonym;
import com.mokujin.ssi.model.internal.Schema;
import com.mokujin.ssi.model.user.response.User;
import com.mokujin.ssi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

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

        credentials.removeAll(nationalCredentials);

        return User.builder()
                .lastName(passport.getLastName())
                .firstName(passport.getFirstName())
                .fatherName(passport.getFatherName())
                .photo(passport.getImage())
                .contacts(identity.getPseudonyms().stream().map(Pseudonym::getContact).collect(Collectors.toList()))
                .credentials(credentials)
                .nationalCredentials(nationalCredentials)
                .build();
    }
}
