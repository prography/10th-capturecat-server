package com.capturecat.core.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import com.capturecat.core.support.handler.CoreExceptionHandler;
import com.capturecat.test.api.RestDocsTest;

import io.restassured.http.ContentType;

import static com.capturecat.test.api.RestDocsUtil.requestPreprocessor;
import static com.capturecat.test.api.RestDocsUtil.responsePreprocessor;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

class HealthCheckControllerTest extends RestDocsTest {

    private HealthCheckController healthCheckController;

    @BeforeEach
    void setUp() {
        healthCheckController = new HealthCheckController();
        mockMvc = mockController(healthCheckController, new CoreExceptionHandler());
    }

    @Test
    void 헬스_체크를_한다() {
        given().contentType(ContentType.JSON)
                .when().get("/health")
                .then()
                .status(HttpStatus.OK)
                .apply(document("healthCheck", requestPreprocessor(), responsePreprocessor(),
                        responseFields(fieldWithPath("status").type(JsonFieldType.STRING).description("애플리케이션 상태"))));
    }
}
