package com.capturecat.core.api.bookmark;

import static com.capturecat.test.api.RestDocsUtil.requestPreprocessor;
import static com.capturecat.test.api.RestDocsUtil.responsePreprocessor;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import io.restassured.http.ContentType;

import com.capturecat.core.service.bookmark.BookmarkService;
import com.capturecat.test.api.RestDocsTest;

class BookmarkControllerTest extends RestDocsTest {

	private BookmarkService bookmarkService;
	private BookmarkController bookmarkController;

	@BeforeEach
	void setUp() {
		bookmarkService = mock(BookmarkService.class);
		bookmarkController = new BookmarkController(bookmarkService);
		mockMvc = mockController(bookmarkController);
	}

	@Test
	void 즐겨찾기를_한다() {
		// given
		willDoNothing().given(bookmarkService).addBookmark(anyLong());

		// when & then
		given().contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.queryParam("imageId", 1L)
			.when().post("/v1/bookmarks")
			.then().status(HttpStatus.OK)
			.apply(document("addBookmark", requestPreprocessor(), responsePreprocessor(),
				queryParameters(parameterWithName("imageId").description("즐겨찾기할 이미지 ID")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 성공 여부"),
					fieldWithPath("data").type(JsonFieldType.NULL).optional().ignored(),
					fieldWithPath("error").type(JsonFieldType.NULL).optional().ignored())));
	}

	@Test
	void 즐겨찾기에서_삭제한다() {
		// given
		willDoNothing().given(bookmarkService).deleteBookmark(anyLong());

		// when & then
		given().contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.queryParam("imageId", 1L)
			.when().delete("/v1/bookmarks")
			.then().status(HttpStatus.OK)
			.apply(document("deleteBookmark", requestPreprocessor(), responsePreprocessor(),
				queryParameters(parameterWithName("imageId").description("즐겨찾기에서 삭제할 이미지 ID")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 성공 여부"),
					fieldWithPath("data").type(JsonFieldType.NULL).optional().ignored(),
					fieldWithPath("error").type(JsonFieldType.NULL).optional().ignored())));
	}
}
