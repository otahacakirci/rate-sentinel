package com.project.ratesentinel.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.project.ratesentinel.domain.RateLimitRule;
import com.project.ratesentinel.repository.RateLimitRuleRepository;

@Service
public class RateLimiterService {

	private static final String KEY_PREFIX = "rate-limit";

	private final RateLimitRuleRepository repository;
	private final StringRedisTemplate redisTemplate;

	public RateLimiterService(
			RateLimitRuleRepository repository,
			StringRedisTemplate redisTemplate) {
		this.repository = repository;
		this.redisTemplate = redisTemplate;
	}

	public boolean isAllowed(String clientId, String endpoint) {
		Optional<RateLimitRule> rule = repository.findByClientIdAndEndpointAndActiveTrue(
			clientId, endpoint);

		if (rule.isEmpty()) {
			return true;
		}

		RateLimitRule activeRule = rule.orElseThrow();
		String key = createKey(clientId, endpoint, activeRule.getWindowSeconds());
		long currentCount = redisTemplate.opsForValue().increment(key);

		if (currentCount == 1L) {
			redisTemplate.expire(key, Duration.ofSeconds(activeRule.getWindowSeconds()));
		}

		return currentCount <= activeRule.getAllowedLimit();
	}

	private String createKey(String clientId, String endpoint, int windowSeconds) {
		long window = Instant.now().getEpochSecond() / windowSeconds;
		return KEY_PREFIX + ":{" + clientId + "}:" + endpoint + ":" + window;
	}
}
