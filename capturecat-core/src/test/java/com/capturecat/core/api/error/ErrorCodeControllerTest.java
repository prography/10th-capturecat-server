package com.capturecat.core.api.error;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.capturecat.core.support.handler.CoreExceptionHandler;
import com.capturecat.test.api.RestDocsTest;
import com.capturecat.test.snippet.ErrorCodeDescriptor;
import com.capturecat.test.snippet.ErrorCodeSnippet;

import io.restassured.http.ContentType;

import static com.capturecat.core.support.error.ErrorType.IMAGE_NOT_FOUND;
import static com.capturecat.core.support.error.ErrorType.INVALID_REQUEST;
import static com.capturecat.core.support.error.ErrorType.TOO_MANY_TAGS;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;

class ErrorCodeControllerTest extends RestDocsTest {

    private ErrorCodeController errorCodeController;

    @BeforeEach
    void setUp() {
        errorCodeController = new ErrorCodeController();
        mockMvc = mockController(errorCodeController, new CoreExceptionHandler());
    }

    @Test
    void 단일_이미지_등록_에러_코드_문서화() {
        List<ErrorCodeDescriptor> errorCodeDescriptors = Stream.of(TOO_MANY_TAGS, IMAGE_NOT_FOUND)
                .map(e -> new ErrorCodeDescriptor(e.getStatus().value(), e.getCode().name(), e.getCode().getMessage()))
                .toList();

        given().contentType(ContentType.JSON)
                .when().get("/v1/error-codes")
                .then()
                .status(HttpStatus.OK)
                .apply(document("errorCode/addTagsToImage", new ErrorCodeSnippet(errorCodeDescriptors)));
    }















    @Test
    void 태그_삭제_에러_코드_문서() {
        List<ErrorCodeDescriptor> errorCodeDescriptors = Stream.of(INVALID_REQUEST, IMAGE_NOT_FOUND)
                .map(e -> new ErrorCodeDescriptor(e.getStatus().value(), e.getCode().name(), e.getCode().getMessage()))
                .toList();

        given().contentType(ContentType.JSON)
                .when().get("/v1/error-codes")
                .then()
                .status(HttpStatus.OK)
                .apply(document("errorCode/removeTagsToImage", new ErrorCodeSnippet(errorCodeDescriptors)));
    }
}
