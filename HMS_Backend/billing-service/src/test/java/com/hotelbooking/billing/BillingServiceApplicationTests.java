package com.hotelbooking.billing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false",
    "spring.cloud.config.fail-fast=false"
})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class BillingServiceApplicationTests {

	@Test
	void contextLoads() {
		// Test that the application context loads successfully
	}

}

