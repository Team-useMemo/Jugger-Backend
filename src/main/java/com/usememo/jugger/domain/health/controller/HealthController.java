package com.usememo.jugger.domain.health.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Tag(name = "헬스 체크용 API", description = "헬스 체크 API에 대한 설명입니다.(백엔드 용)")
@RequestMapping("/health")
public class HealthController {

	@GetMapping("/check")
	public ResponseEntity<String> check() {
		return ResponseEntity.ok("OK");
	}

}
