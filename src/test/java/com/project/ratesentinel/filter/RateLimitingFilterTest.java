package com.project.ratesentinel.filter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
class RateLimitingFilterTest {

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
	void rejectsRequestWithoutClientIdHeader() throws Exception {
		mockMvc.perform(get("/api/resource"))
			.andExpect(status().isBadRequest())
			.andExpect(content().string("Missing X-Client-Id header"));
	}

	@Test
	void rejectsRequestAfterRateLimitIsExceeded() throws Exception {
		repository.save(new RateLimitRule("web-client", "/api/resource", 2, 60, true));

		mockMvc.perform(get("/api/resource").header("X-Client-Id", "web-client"))
			.andExpect(status().isOk())
			.andExpect(content().string("Success"));
		mockMvc.perform(get("/api/resource").header("X-Client-Id", "web-client"))
			.andExpect(status().isOk())
			.andExpect(content().string("Success"));
		mockMvc.perform(get("/api/resource").header("X-Client-Id", "web-client"))
			.andExpect(status().isTooManyRequests())
			.andExpect(content().string("Too Many Requests - Rate limit exceeded"));
	}
}
