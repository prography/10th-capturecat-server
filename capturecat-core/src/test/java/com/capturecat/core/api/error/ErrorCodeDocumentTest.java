package com.capturecat.core.api.error;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpStatus;

import io.restassured.http.ContentType;

import com.capturecat.core.support.error.ErrorType;
import com.capturecat.test.api.RestDocsTest;
import com.capturecat.test.snippet.ErrorCodeDescriptor;
import com.capturecat.test.snippet.ErrorCodeSnippet;

public abstract class ErrorCodeDocumentTest extends RestDocsTest {

	private ErrorCodeController errorCodeController;

	@BeforeEach
	void setUp() {
		errorCodeController = new ErrorCodeController();
		mockMvc = mockController(errorCodeController);
	}

	public void generateErrorDocs(String identifier, List<ErrorCodeDescriptor> errorCodeDescriptors) {
		given().contentType(ContentType.JSON)
			.when().get("/v1/error-codes")
			.then().status(HttpStatus.OK)
			.apply(document(identifier, new ErrorCodeSnippet(errorCodeDescriptors)));
	}

	public List<ErrorCodeDescriptor> generateErrorCodeDescriptors(ErrorType... errorTypes) {
		return Stream.of(errorTypes)
			.map(e -> new ErrorCodeDescriptor(e.getStatus().value(), e.getCode().name(), e.getCode().getMessage()))
			.toList();
	}
}
