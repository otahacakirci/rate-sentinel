package com.project.ratesentinel.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.project.ratesentinel.domain.RateLimitRule;
import com.project.ratesentinel.repository.RateLimitRuleRepository;

@RestController
@RequestMapping("/api/rules")
public class RateLimitRuleController {

	private final RateLimitRuleRepository repository;

	public RateLimitRuleController(RateLimitRuleRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	public List<RateLimitRule> listRules() {
		return repository.findAll();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public RateLimitRule createRule(@RequestBody RateLimitRule rule) {
		return repository.save(rule);
	}
}
