package com.project.ratesentinel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "rate_limit_rules",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_rate_limit_rule_client_endpoint",
		columnNames = { "client_id", "endpoint" }))
public class RateLimitRule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "client_id", nullable = false)
	private String clientId;

	@Column(name = "endpoint", nullable = false)
	private String endpoint;

	@Column(name = "allowed_limit", nullable = false)
	private Integer allowedLimit;

	@Column(name = "window_seconds", nullable = false)
	private Integer windowSeconds;

	@Column(name = "active", nullable = false)
	private Boolean active;

	protected RateLimitRule() {
	}

	public RateLimitRule(
			String clientId,
			String endpoint,
			Integer allowedLimit,
			Integer windowSeconds,
			Boolean active) {
		this.clientId = clientId;
		this.endpoint = endpoint;
		this.allowedLimit = allowedLimit;
		this.windowSeconds = windowSeconds;
		this.active = active;
	}

	public Long getId() {
		return id;
	}

	public String getClientId() {
		return clientId;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public Integer getAllowedLimit() {
		return allowedLimit;
	}

	public Integer getWindowSeconds() {
		return windowSeconds;
	}

	public Boolean getActive() {
		return active;
	}
}
