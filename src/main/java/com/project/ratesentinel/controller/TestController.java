package com.project.ratesentinel.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	@GetMapping("/api/resource")
	public String resource() {
		return "Success";
	}
}
