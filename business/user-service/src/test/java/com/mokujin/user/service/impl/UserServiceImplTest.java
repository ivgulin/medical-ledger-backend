package com.mokujin.user.service.impl;

import com.mokujin.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserServiceImpl userService;


    @Test
    void get_validInputs_userIsReturned() {
        User user = new User();
        user.setFirstName("name");

        when(restTemplate.getForObject(anyString(), any())).thenReturn(user);

        User result = userService.get("test", "test");

        assertEquals(user, result);
    }

    @Test
    void delete_validInputs_methodIsExecuted() {
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        String publicKey = "publicKey";
        String privateKey = "privateKey";

        userService.delete(publicKey, privateKey);

        verify(restTemplate, times(1)).delete(urlCaptor.capture());

        assertTrue(urlCaptor.getValue().contains(publicKey));
        assertTrue(urlCaptor.getValue().contains(privateKey));
    }
}