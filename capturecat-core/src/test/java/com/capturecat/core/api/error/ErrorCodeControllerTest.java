package com.capturecat.core.api.error;

import static com.capturecat.core.support.error.ErrorType.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.restassured.http.ContentType;

import com.capturecat.core.support.error.ErrorType;
import com.capturecat.test.api.RestDocsTest;
import com.capturecat.test.snippet.ErrorCodeDescriptor;
import com.capturecat.test.snippet.ErrorCodeSnippet;

class ErrorCodeControllerTest extends RestDocsTest {

	private ErrorCodeController errorCodeController;

	private static List<ErrorCodeDescriptor> generateErrorCodeDescriptors(ErrorType... errorTypes) {
		return Stream.of(errorTypes)
			.map(e -> new ErrorCodeDescriptor(e.getStatus().value(), e.getCode().name(), e.getCode().getMessage()))
			.toList();
	}

	@BeforeEach
	void setUp() {
		errorCodeController = new ErrorCodeController();
		mockMvc = mockController(errorCodeController);
	}

	@Test
	void 단일_이미지_태그_등록_에러_코드_문서화() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(INSUFFICIENT_TAG_COUNT,
			TOO_MANY_TAGS, DUPLICATE_TAG_NAMES, IMAGE_NOT_FOUND);
		generateErrorDocs("errorCode/addTagsToImage", errorCodeDescriptors);
	}

	@Test
	void 이미지_업로드_에러_코드_문서() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(INVALID_IMAGE_FORMAT,
			INSUFFICIENT_TAG_COUNT, DUPLICATE_TAG_NAMES, TAG_INFO_MISMATCH, IMAGE_UPLOAD_FAILED);
		generateErrorDocs("errorCode/upload", errorCodeDescriptors);
	}

	@Test
	void 태그_삭제_에러_코드_문서() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(INVALID_REQUEST, IMAGE_NOT_FOUND);
		generateErrorDocs("errorCode/removeTagsToImage", errorCodeDescriptors);
	}

	private void generateErrorDocs(String identifier, List<ErrorCodeDescriptor> errorCodeDescriptors) {
		given().contentType(ContentType.JSON)
			.when().get("/v1/error-codes")
			.then().status(HttpStatus.OK)
			.apply(document(identifier, new ErrorCodeSnippet(errorCodeDescriptors)));
	}

}
