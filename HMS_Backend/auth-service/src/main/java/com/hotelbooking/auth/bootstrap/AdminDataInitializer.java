package com.hotelbooking.auth.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.hotelbooking.auth.config.AdminUserProperties;
import com.hotelbooking.auth.domain.Role;
import com.hotelbooking.auth.domain.User;
import com.hotelbooking.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminUserProperties adminProps;

    @Override
    public void run(String... args) {

        if (userRepository.findByEmail(adminProps.getEmail()).isEmpty()) {

            User admin = User.builder()
                    .email(adminProps.getEmail())
                    .password(adminProps.getPassword()) // hash in service if possible
                    .fullName(adminProps.getFullName())
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();

            userRepository.save(admin);
        }
    }
}