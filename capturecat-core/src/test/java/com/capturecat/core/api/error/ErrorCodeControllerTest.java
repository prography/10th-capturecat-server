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

	@BeforeEach
	void setUp() {
		errorCodeController = new ErrorCodeController();
		mockMvc = mockController(errorCodeController);
	}

	@Test
	void 단일_이미지_태그_등록_에러_코드_문서화() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(TOO_MANY_TAGS,
			DUPLICATE_TAG_NAMES, ALREADY_REGISTERED_TAGS, IMAGE_NOT_FOUND);
		generateErrorDocs("errorCode/addTagsToImage", errorCodeDescriptors);
	}

	@Test
	void 이미지_업로드_에러_코드_문서() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(UPLOAD_METADATA_MISMATCH,
			INVALID_DATE_FORMAT, USER_NOT_FOUND, IMAGE_UPLOAD_FAILED);
		generateErrorDocs("errorCode/upload", errorCodeDescriptors);
	}

	@Test
	void 태그를_포함한_이미지_목록_조회_에러_코드_문서() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(IMAGE_NOT_FOUND);
		generateErrorDocs("errorCode/getImagesWithTagsByUser", errorCodeDescriptors);
	}

	@Test
	void 태그_삭제_에러_코드_문서() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(IMAGE_NOT_FOUND,
			USER_NOT_FOUND, IMAGE_TAG_NOT_FOUND);
		generateErrorDocs("errorCode/removeTagToImage", errorCodeDescriptors);
	}

	@Test
	void 태그_목록_조회_에러_코드_문서() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(USER_NOT_FOUND);
		generateErrorDocs("errorCode/getTags", errorCodeDescriptors);
	}

	@Test
	void 토큰_재발행_에러_코드_문서() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(INVALID_REFRESH_TOKEN,
			REFRESH_TOKEN_EXPIRED);
		generateErrorDocs("errorCode/reissue", errorCodeDescriptors);
	}

	@Test
	void 태그로_이미지_검색_에러_코드_문서() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(USER_NOT_FOUND);
		generateErrorDocs("errorCode/searchImagesByTags", errorCodeDescriptors);
	}

	@Test
	void 소셜_로그인_회원가입_에러_코드_문서() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(INVALID_ID_TOKEN);
		generateErrorDocs("errorCode/oauthLogin", errorCodeDescriptors);
	}

	@Test
	void 로그아웃_에러_코드_문서() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(INVALID_REFRESH_TOKEN,
			REFRESH_TOKEN_EXPIRED);
		generateErrorDocs("errorCode/logout", errorCodeDescriptors);
	}

	@Test
	void 이미지_단건_조회_에러_코드_문서() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(IMAGE_ACCESS_DENIED,
			USER_NOT_FOUND, IMAGE_NOT_FOUND);
		generateErrorDocs("errorCode/getImageWithTags", errorCodeDescriptors);
	}

	@Test
	void 이미지_삭제_에러_코드_문서() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(IMAGE_ACCESS_DENIED,
			USER_NOT_FOUND, IMAGE_NOT_FOUND, IMAGE_DELETE_FAILED);
		generateErrorDocs("errorCode/removeImage", errorCodeDescriptors);
	}

	@Test
	void 이미지_즐겨찾기_등록_에러_코드_문서() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(BOOKMARK_DUPLICATION,
			USER_NOT_FOUND, IMAGE_NOT_FOUND);
		generateErrorDocs("errorCode/addBookmark", errorCodeDescriptors);
	}

	@Test
	void 이미지_즐겨찾기_삭제_에러_코드_문서() {
		List<ErrorCodeDescriptor> errorCodeDescriptors = generateErrorCodeDescriptors(USER_NOT_FOUND,
			IMAGE_NOT_FOUND, BOOKMARK_NOT_FOUND);
		generateErrorDocs("errorCode/deleteBookmark", errorCodeDescriptors);
	}

	private void generateErrorDocs(String identifier, List<ErrorCodeDescriptor> errorCodeDescriptors) {
		given().contentType(ContentType.JSON)
			.when().get("/v1/error-codes")
			.then().status(HttpStatus.OK)
			.apply(document(identifier, new ErrorCodeSnippet(errorCodeDescriptors)));
	}

	private List<ErrorCodeDescriptor> generateErrorCodeDescriptors(ErrorType... errorTypes) {
		return Stream.of(errorTypes)
			.map(e -> new ErrorCodeDescriptor(e.getStatus().value(), e.getCode().name(), e.getCode().getMessage()))
			.toList();
	}
}
