package com.mokujin.user.service.impl;

import com.mokujin.user.model.User;
import com.mokujin.user.model.document.Document;
import com.mokujin.user.model.document.impl.Certificate;
import com.mokujin.user.model.document.impl.Diploma;
import com.mokujin.user.model.document.impl.NationalNumber;
import com.mokujin.user.model.document.impl.NationalPassport;
import com.mokujin.user.model.notification.Notification;
import com.mokujin.user.model.presentation.Affirmation;
import com.mokujin.user.model.presentation.PresentationAttributes;
import com.mokujin.user.model.presentation.PresentationRequest;
import com.mokujin.user.model.presentation.Proof;
import com.mokujin.user.service.NotificationService;
import com.mokujin.user.service.PresentationService;
import com.mokujin.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static com.mokujin.user.model.User.Role;
import static com.mokujin.user.model.document.Document.Type;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresentationServiceImpl implements PresentationService {

    private final NotificationService notificationService;
    private final UserService userService;
    private final RestTemplate restTemplate;

    @Override
    public PresentationAttributes getPresentationAttributes(Role role) {

        PresentationAttributes presentationAttributes = new PresentationAttributes();

        Arrays.stream(NationalPassport.class.getDeclaredFields())
                .map(Field::getName)
                .forEach(presentationAttributes::addPassportAttribute);

        Arrays.stream(NationalNumber.class.getDeclaredFields())
                .map(Field::getName)
                .forEach(presentationAttributes::addNationalNumberAttribute);

        if (Role.DOCTOR.equals(role)) {
            Arrays.stream(Diploma.class.getDeclaredFields())
                    .map(Field::getName)
                    .forEach(presentationAttributes::addDiplomaAttribute);

            Arrays.stream(Certificate.class.getDeclaredFields())
                    .map(Field::getName)
                    .forEach(presentationAttributes::addCertificateAttribute);
        }

        return presentationAttributes;
    }

    @Override
    public void requestPresentation(String publicKey, String privateKey, PresentationRequest presentationRequest,
                                    String connectionNumber) {

        User user = userService.get(publicKey, privateKey);

        List<String> passportAttributes = presentationRequest.getPassportAttributes();
        List<String> nationalNumberAttributes = presentationRequest.getNationalNumberAttributes();
        List<String> diplomaAttributes = presentationRequest.getDiplomaAttributes();
        List<String> certificateAttributes = presentationRequest.getCertificateAttributes();

        if (!passportAttributes.isEmpty()) {
            Notification notification = notificationService.addPresentationNotification(user, passportAttributes,
                    Type.passport.name(), connectionNumber);
            log.info("passport notification =  '{}'", notification);
        }

        if (!nationalNumberAttributes.isEmpty()) {
            Notification notification = notificationService.addPresentationNotification(user, nationalNumberAttributes,
                    Type.number.name(), connectionNumber);
            log.info("national number notification =  '{}'", notification);
        }

        if (!diplomaAttributes.isEmpty()) {
            Notification notification = notificationService.addPresentationNotification(user, diplomaAttributes,
                    Type.diploma.name(), connectionNumber);
            log.info("diploma notification =  '{}'", notification);
        }

        if (!certificateAttributes.isEmpty()) {
            Notification notification = notificationService.addPresentationNotification(user, certificateAttributes,
                    Type.certificate.name(), connectionNumber);
            log.info("certificate notification =  '{}'", notification);
        }
    }

    @Override
    public void presentProof(String publicKey, String privateKey, Document document, String connectionNumber) {

        String url = "http://self-sovereign-identity-service/verification/present?public="
                + publicKey + "&private=" + privateKey;
        Proof proof = restTemplate.postForObject(url, document, Proof.class);
        log.info("proof =  '{}'", proof);

        User user = userService.get(publicKey, privateKey);

        notificationService.removePresentationNotification(user, connectionNumber);

        Notification notification = notificationService.addProofNotification(user, proof, connectionNumber);
        log.info("proof notification =  '{}'", notification);
    }

    @Override
    public Affirmation verifyProof(Proof proof, String nationalNumber, String connectionNumber) {

        String url = "http://self-sovereign-identity-service/verification/verify";
        Affirmation affirmation = restTemplate.postForObject(url, proof, Affirmation.class);

        notificationService.removeProofNotification(nationalNumber, connectionNumber);

        return affirmation;
    }
}
