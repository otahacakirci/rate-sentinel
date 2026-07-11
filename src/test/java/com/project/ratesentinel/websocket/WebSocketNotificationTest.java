package com.project.ratesentinel.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.project.ratesentinel.domain.RateLimitRule;
import com.project.ratesentinel.event.RateLimitViolationEvent;
import com.project.ratesentinel.repository.RateLimitRuleRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class WebSocketNotificationTest {

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

	@MockitoSpyBean
	private SimpMessagingTemplate messagingTemplate;

	@BeforeEach
	void resetState() {
		repository.deleteAll();
		clearInvocations(messagingTemplate);
	}

	@Test
	void loadsMessagingTemplate() {
		assertNotNull(messagingTemplate);
	}

	@Test
	void publishesViolationWhenFilterRejectsRequest() throws Exception {
		repository.save(new RateLimitRule("notification-client", "/api/resource", 1, 60, true));

		mockMvc.perform(get("/api/resource").header("X-Client-Id", "notification-client"))
			.andExpect(status().isOk());
		mockMvc.perform(get("/api/resource").header("X-Client-Id", "notification-client"))
			.andExpect(status().isTooManyRequests());

		ArgumentCaptor<RateLimitViolationEvent> eventCaptor = ArgumentCaptor.forClass(
			RateLimitViolationEvent.class);
		verify(messagingTemplate).convertAndSend(eq("/topic/violations"), eventCaptor.capture());

		RateLimitViolationEvent event = eventCaptor.getValue();
		assertEquals("notification-client", event.clientId());
		assertEquals("/api/resource", event.endpoint());
		assertNotNull(event.timestamp());
	}
}
