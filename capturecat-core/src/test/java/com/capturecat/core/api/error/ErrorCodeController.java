package com.capturecat.core.api.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/error-codes")
public class ErrorCodeController {

	@GetMapping
	public ResponseEntity<?> errorCodes() {
		return ResponseEntity.ok().build();
	}

}
