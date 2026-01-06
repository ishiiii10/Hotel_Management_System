package com.hotelbooking.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
	"eureka.client.enabled=false",
	"spring.cloud.config.enabled=false",
	"spring.config.import=",
	"auth.jwt.secret=test-secret-key-for-testing-only-123456789012345678901234567890",
	"auth.jwt.expiry-minutes=60"
})
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
