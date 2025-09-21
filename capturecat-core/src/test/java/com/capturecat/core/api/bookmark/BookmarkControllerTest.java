package com.capturecat.core.api.bookmark;

import static com.capturecat.test.api.RestDocsUtil.requestPreprocessor;
import static com.capturecat.test.api.RestDocsUtil.responsePreprocessor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import io.restassured.http.ContentType;

import com.capturecat.core.DummyObject;
import com.capturecat.core.service.bookmark.BookmarkService;
import com.capturecat.core.service.image.ImageWithTagsResponse;
import com.capturecat.core.service.image.TagResponse;
import com.capturecat.core.support.util.CursorUtil;
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
		willDoNothing().given(bookmarkService).addBookmark(anyLong(), any());

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
	void 즐겨찾기한_이미지를_조회한다() {
		// given
		BDDMockito.given(bookmarkService.getBookmarkImages(any(), any())).willReturn(
			CursorUtil.toCursorResponse(
				List.of(ImageWithTagsResponse.from(DummyObject.newMockImage(1L))),
				false,
				ImageWithTagsResponse::id));

		// when & then
		given().contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.queryParam("page", 0)
			.queryParam("size", 10)
			.when().get("/v1/bookmarks/images")
			.then().status(HttpStatus.OK).log().all()
			.apply(document("getBookmarkImages", requestPreprocessor(), responsePreprocessor(),
				queryParameters(
					parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
					parameterWithName("size").description("페이지 크기 (기본값: 10, 최대: 100)").optional()),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"),
					fieldWithPath("data.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 여부"),
					fieldWithPath("data.lastCursor").type(JsonFieldType.NUMBER).description("마지막 커서 ID"),
					fieldWithPath("data.items").type(JsonFieldType.ARRAY).description("즐겨찾기한 이미지 목록"),
					fieldWithPath("data.items[].id").type(JsonFieldType.NUMBER).description("이미지 ID"),
					fieldWithPath("data.items[].name").type(JsonFieldType.STRING).description("이미지 이름"),
					fieldWithPath("data.items[].url").type(JsonFieldType.STRING).description("이미지 URL"),
					fieldWithPath("data.items[].captureDate").type(JsonFieldType.STRING).description("캡처한 날짜"),
					fieldWithPath("data.items[].isBookmarked").type(JsonFieldType.BOOLEAN).description("즐겨찾기 여부"),
					fieldWithPath("error").type(JsonFieldType.NULL).optional().ignored())));
	}

	@Test
	void 즐겨찾기한_이미지의_이미지태그를_조회한다() {
		// given
		BDDMockito.given(bookmarkService.getBookmarkImageTags(any(), any())).willReturn(
			CursorUtil.toCursorResponse(List.of(new TagResponse(1L, "고양이"), new TagResponse(2L, "cat")),
				false,
				TagResponse::id));

		// when & then
		given().contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.queryParam("page", 0)
			.queryParam("size", 10)
			.when().get("/v1/bookmarks/tags")
			.then().status(HttpStatus.OK).log().all()
			.apply(document("getBookmarkImageTags", requestPreprocessor(), responsePreprocessor(),
				queryParameters(
					parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
					parameterWithName("size").description("페이지 크기 (기본값: 10, 최대: 100)").optional()),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"),
					fieldWithPath("data.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 여부"),
					fieldWithPath("data.lastCursor").type(JsonFieldType.NUMBER).description("마지막 커서 ID"),
					fieldWithPath("data.items").type(JsonFieldType.ARRAY).description("즐겨찾기한 이미지 태그 목록"),
					fieldWithPath("data.items[].id").type(JsonFieldType.NUMBER).description("이미지 태그 ID"),
					fieldWithPath("data.items[].name").type(JsonFieldType.STRING).description("이미지 태그 이름"),
					fieldWithPath("error").type(JsonFieldType.NULL).optional().ignored())));
	}

	@Test
	void 즐겨찾기에서_삭제한다() {
		// given
		willDoNothing().given(bookmarkService).deleteBookmark(anyLong(), any());

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
