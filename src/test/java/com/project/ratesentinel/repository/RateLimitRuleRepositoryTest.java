package com.project.ratesentinel.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.project.ratesentinel.domain.RateLimitRule;

@SpringBootTest
@Testcontainers
class RateLimitRuleRepositoryTest {

	@Container
	private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
		DockerImageName.parse("postgres:15-alpine"));

	@DynamicPropertySource
	static void datasourceProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
	}

	@Autowired
	private RateLimitRuleRepository repository;

	@BeforeEach
	void clearRules() {
		repository.deleteAll();
	}

	@Test
	void findsSavedActiveRule() {
		RateLimitRule savedRule = repository.save(
			new RateLimitRule("client-1", "/api/orders", 100, 60, true));

		Optional<RateLimitRule> result = repository.findByClientIdAndEndpointAndActiveTrue(
			"client-1", "/api/orders");

		assertTrue(result.isPresent());
		assertEquals(savedRule.getId(), result.orElseThrow().getId());
		assertEquals(100, result.orElseThrow().getAllowedLimit());
		assertEquals(60, result.orElseThrow().getWindowSeconds());
	}

	@Test
	void doesNotFindInactiveRule() {
		repository.save(new RateLimitRule("client-2", "/api/payments", 25, 30, false));

		Optional<RateLimitRule> result = repository.findByClientIdAndEndpointAndActiveTrue(
			"client-2", "/api/payments");

		assertTrue(result.isEmpty());
	}
}
