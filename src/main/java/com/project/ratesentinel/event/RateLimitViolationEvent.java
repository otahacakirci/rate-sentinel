package com.project.ratesentinel.event;

import java.time.Instant;

public record RateLimitViolationEvent(String clientId, String endpoint, Instant timestamp) {
}
