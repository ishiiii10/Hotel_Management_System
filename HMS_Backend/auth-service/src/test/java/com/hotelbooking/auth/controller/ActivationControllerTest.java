package com.hotelbooking.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hotelbooking.auth.service.UserService;

@ExtendWith(MockitoExtension.class)
class ActivationControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private ActivationController activationController;

    @Test
    void testActivate_Success() {
        doNothing().when(userService).activateUser(anyString());

        ResponseEntity<String> response = activationController.activate("test-token-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account activated successfully", response.getBody());
    }
}

