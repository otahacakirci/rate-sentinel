package com.project.ratesentinel.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.project.ratesentinel.domain.RateLimitRule;
import com.project.ratesentinel.repository.RateLimitRuleRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RateLimitRuleControllerTest {

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
	private MockMvc mockMvc;

	@Autowired
	private RateLimitRuleRepository repository;

	@BeforeEach
	void clearRules() {
		repository.deleteAll();
	}

	@Test
	void createsRuleWithoutClientIdHeader() throws Exception {
		String requestBody = """
			{
			  "clientId": "managed-client",
			  "endpoint": "/api/orders",
			  "allowedLimit": 10,
			  "windowSeconds": 60,
			  "active": true
			}
			""";

		mockMvc.perform(post("/api/rules")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.clientId").value("managed-client"));

		assertEquals(1, repository.count());
		assertEquals("managed-client", repository.findAll().get(0).getClientId());
	}

	@Test
	void listsRulesWithoutClientIdHeader() throws Exception {
		repository.save(new RateLimitRule("listed-client", "/api/payments", 20, 30, true));

		mockMvc.perform(get("/api/rules"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].clientId").value("listed-client"))
			.andExpect(jsonPath("$[0].endpoint").value("/api/payments"));
	}
}
