package com.mokujin.user.service.impl;

import com.mokujin.user.model.Contact;
import com.mokujin.user.model.ProcessedUserCredentials;
import com.mokujin.user.model.User;
import com.mokujin.user.model.notification.SystemNotification;
import com.mokujin.user.service.NotificationService;
import com.mokujin.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static com.mokujin.user.model.notification.Notification.Type.INVITATION;
import static com.mokujin.user.model.notification.NotificationConstants.INVITATION_CONTENT_EN;
import static com.mokujin.user.model.notification.NotificationConstants.INVITATION_CONTENT_UKR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private InvitationServiceImpl invitationService;

    @Test
    void inviteBack_validInputs_userIsReturned() {

        String publicKey = "public";
        String privateKey = "private";
        Contact doctor = Contact.builder()
                .contactName("doctor")
                .build();

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");

        when(userService.get(publicKey, privateKey)).thenReturn(user);
        SystemNotification notification = new SystemNotification(123L, INVITATION, Contact.builder().build(),
                "", "", INVITATION_CONTENT_EN, INVITATION_CONTENT_UKR);
        when(notificationService.addInviteNotification(publicKey, privateKey, doctor, user)).thenReturn(notification);

        User result = invitationService.inviteBack(publicKey, privateKey, doctor);

        assertEquals(user, result);
    }

    @Test
    void accept_validInputs_userIsReturned() {

        String publicKey = "public";
        String privateKey = "private";
        String number = "number";

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");

        ProcessedUserCredentials processedUserCredentials = ProcessedUserCredentials.builder()
                .publicKey(publicKey)
                .privateKey(privateKey)
                .build();
        when(notificationService.removeInviteNotification(number, number))
                .thenReturn(processedUserCredentials);

        ArgumentCaptor<ProcessedUserCredentials> credentialsCaptor = ArgumentCaptor.forClass(ProcessedUserCredentials.class);

        when(restTemplate.postForObject(anyString(), credentialsCaptor.capture(), any())).thenReturn(user);

        User result = invitationService.accept(publicKey, privateKey, number, number);

        assertEquals(user, result);
        assertEquals(processedUserCredentials, credentialsCaptor.getValue());

    }

    @Test
    void decline_validInputs_methodIsExecuted() {

        String number = "number";

        ArgumentCaptor<String> doctorNumberCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> patientNumberCaptor = ArgumentCaptor.forClass(String.class);

        when(notificationService.removeInviteNotification(doctorNumberCaptor.capture(), patientNumberCaptor.capture()))
                .thenReturn(null);

        invitationService.decline(number, number);

        assertEquals(doctorNumberCaptor.getValue(), number);
        assertEquals(patientNumberCaptor.getValue(), number);

    }
}