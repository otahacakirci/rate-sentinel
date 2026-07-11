package com.project.ratesentinel.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.project.ratesentinel.domain.RateLimitRule;
import com.project.ratesentinel.repository.RateLimitRuleRepository;

@SpringBootTest
@Testcontainers
class RateLimiterServiceTest {

	private static final int REDIS_PORT = 6379;

	@Container
	private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
		DockerImageName.parse("postgres:15-alpine"));

	@Container
	private static final GenericContainer<?> REDIS = new GenericContainer<>(
		DockerImageName.parse("redis:7.2-alpine"))
		.withExposedPorts(REDIS_PORT);

	@DynamicPropertySource
	static void infrastructureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
		registry.add("spring.data.redis.host", REDIS::getHost);
		registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(REDIS_PORT));
	}

	@Autowired
	private RateLimiterService rateLimiterService;

	@Autowired
	private RateLimitRuleRepository repository;

	@BeforeEach
	void clearRules() {
		repository.deleteAll();
	}

	@Test
	void allowsRequestWhenRuleDoesNotExist() {
		assertTrue(rateLimiterService.isAllowed("unconfigured-client", "/api/status"));
	}

	@Test
	void rejectsRequestAfterAllowedLimitIsExceeded() {
		repository.save(new RateLimitRule("limited-client", "/api/orders", 3, 60, true));

		assertTrue(rateLimiterService.isAllowed("limited-client", "/api/orders"));
		assertTrue(rateLimiterService.isAllowed("limited-client", "/api/orders"));
		assertTrue(rateLimiterService.isAllowed("limited-client", "/api/orders"));
		assertFalse(rateLimiterService.isAllowed("limited-client", "/api/orders"));
	}
}
