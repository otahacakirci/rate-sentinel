package com.project.ratesentinel;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class EnvironmentControlTest {

	@Container
	private static final GenericContainer<?> CONTAINER = new GenericContainer<>(
		DockerImageName.parse("alpine:latest"))
		.withCommand("tail", "-f", "/dev/null");

	@Test
	void containerIsRunning() {
		assertTrue(CONTAINER.isRunning());
	}
}