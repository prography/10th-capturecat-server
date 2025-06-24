package com.capturecat.core.api;

import static com.capturecat.test.api.RestDocsUtil.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import io.restassured.http.ContentType;

import com.capturecat.test.api.RestDocsTest;

class HealthCheckControllerTest extends RestDocsTest {

	private HealthCheckController healthCheckController;

	@BeforeEach
	void setUp() {
		healthCheckController = new HealthCheckController();
		mockMvc = mockController(healthCheckController);
	}

	@Test
	void 헬스_체크를_한다() {
		given().contentType(ContentType.JSON)
			.when()
			.get("/health")
			.then()
			.status(HttpStatus.OK)
			.apply(document("healthCheck", requestPreprocessor(), responsePreprocessor(),
					responseFields(fieldWithPath("status").type(JsonFieldType.STRING).description("애플리케이션 상태"))));
	}

}
