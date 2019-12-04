package com.mokujin.user.service.impl;

import com.mokujin.user.model.User;
import com.mokujin.user.model.record.HealthRecord;
import com.mokujin.user.model.record.impl.BodyMeasurement;
import com.mokujin.user.model.record.impl.HeartHealthRecord;
import com.mokujin.user.service.NotificationService;
import com.mokujin.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthDataServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private HealthDataServiceImpl healthDataService;

    @Test
    void save_validInputs_recordsAreReturned() {

        BodyMeasurement input = new BodyMeasurement();

        HealthRecord[] records = new HealthRecord[2];
        records[0] = new HeartHealthRecord();
        records[1] = input;

        when(restTemplate.postForObject(anyString(), any(), any())).thenReturn(records);

        List<HealthRecord> expected = Arrays.asList(records);

        List<HealthRecord> result = healthDataService.save("", "", input);

        assertEquals(expected, result);
    }

    @Test
    void share_validInputs_userIsReturned() {
        User patient = new User();
        patient.setFirstName("name");

        HeartHealthRecord record = new HeartHealthRecord();
        String doctorNumber = "number";

        String publicKey = "public";
        String privateKey = "private";

        when(userService.get(publicKey, privateKey)).thenReturn(patient);
        when(notificationService.addHealthNotification(patient, record, doctorNumber)).thenReturn(null);

        User result = healthDataService.share(publicKey, privateKey, record, doctorNumber);

        assertEquals(patient, result);
        verify(notificationService, times(1)).addHealthNotification(patient, record, doctorNumber);
    }

}