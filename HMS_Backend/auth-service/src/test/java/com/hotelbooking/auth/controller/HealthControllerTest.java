package com.hotelbooking.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

    @InjectMocks
    private HealthController healthController;

    @Test
    void testPing() {
        String result = healthController.ping();

        assertEquals("Auth service is reachable", result);
    }
}

