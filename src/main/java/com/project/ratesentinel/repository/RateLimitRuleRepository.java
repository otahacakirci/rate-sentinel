package com.project.ratesentinel.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.ratesentinel.domain.RateLimitRule;

public interface RateLimitRuleRepository extends JpaRepository<RateLimitRule, Long> {

	Optional<RateLimitRule> findByClientIdAndEndpointAndActiveTrue(String clientId, String endpoint);
}
