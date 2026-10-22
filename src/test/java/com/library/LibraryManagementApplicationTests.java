package com.library;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Full-context smoke test. It needs a reachable PostgreSQL database plus DB_PASSWORD and
 * JWT_SECRET (see .env.example), so it only runs when RUN_INTEGRATION_TESTS=true.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_INTEGRATION_TESTS", matches = "true")
class LibraryManagementApplicationTests {

	@Test
	void contextLoads() {
	}

}
