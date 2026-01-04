package com.hotelbooking.auth.exception;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.hotelbooking.auth.controller.AuthController;
import com.hotelbooking.auth.domain.AuthErrorCode;
import com.hotelbooking.auth.security.JwtUtil;
import com.hotelbooking.auth.service.UserService;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalAuthExceptionHandlerTest.MockConfig.class)
class GlobalAuthExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService mockUserService;

    @Test
    void whenInvalidCredentials_thenReturns401() throws Exception {
        when(mockUserService.authenticate(anyString(), anyString()))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "test@test.com",
                              "password": "wrong"
                            }
                        """)
        )
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code")
                .value(AuthErrorCode.INVALID_CREDENTIALS.name()));
    }

    @Test
    void whenAccountDisabled_thenReturns403() throws Exception {
        when(mockUserService.authenticate(anyString(), anyString()))
                .thenThrow(new AccountDisabledException());

        mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "test@test.com",
                              "password": "Password1"
                            }
                        """)
        )
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error.code")
                .value(AuthErrorCode.ACCOUNT_DISABLED.name()));
    }

    @Test
    void whenValidationFails_thenReturns400() throws Exception {
        mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "invalid",
                              "password": ""
                            }
                        """)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code")
                .value(AuthErrorCode.VALIDATION_ERROR.name()));
    }

    @Configuration
    static class MockConfig {

        @Bean
        UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        JwtUtil jwtUtil() {
            return mock(JwtUtil.class);
        }
    }
}