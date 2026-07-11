package com.project.ratesentinel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
	+ "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
class RateSentinelApplicationTests {

	@Test
	void contextLoads() {
	}

}
