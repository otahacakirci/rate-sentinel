package com.project.ratesentinel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.project.ratesentinel.repository.RateLimitRuleRepository;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
	+ "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
class RateSentinelApplicationTests {

	@MockitoBean
	private RateLimitRuleRepository repository;

	@Test
	void contextLoads() {
	}

}
