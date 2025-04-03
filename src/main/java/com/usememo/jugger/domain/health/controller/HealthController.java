package com.usememo.jugger.domain.health.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/health")
public class HealthController {

	@GetMapping("/check")
	public ResponseEntity<String> check() {
		return ResponseEntity.ok("OK");
	}

}
