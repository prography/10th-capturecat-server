package com.capturecat.core.api.tag;

import static com.capturecat.test.api.RestDocsUtil.requestPreprocessor;
import static com.capturecat.test.api.RestDocsUtil.responsePreprocessor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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

import com.capturecat.core.service.image.TagResponse;
import com.capturecat.core.service.tag.TagService;
import com.capturecat.core.support.response.CursorResponse;
import com.capturecat.test.api.RestDocsTest;

class TagControllerTest extends RestDocsTest {

	private TagService tagService;
	private TagController tagController;

	@BeforeEach
	void setUp() {
		tagService = mock(TagService.class);
		tagController = new TagController(tagService);
		mockMvc = mockController(tagController);
	}

	@Test
	void 사용자가_등록한_태그_목록을_조회한다() {
		// given
		BDDMockito.given(tagService.getTags(any(), any()))
			.willReturn(new CursorResponse<>(false, 1L, List.of(
				new TagResponse(1L, "tagName")
			)));

		// when & then
		given().contentType(ContentType.JSON)
			.param("page", 0)
			.param("size", 10)
			.when().get("/v1/tags")
			.then().status(HttpStatus.OK)
			.apply(document("getTags", requestPreprocessor(), responsePreprocessor(),
				queryParameters(
					parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
					parameterWithName("size").description("페이지 크기 (기본값: 10)").optional()
				),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"),
					fieldWithPath("data.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 여부"),
					fieldWithPath("data.lastCursor").type(JsonFieldType.NUMBER).description("마지막 커서 ID"),
					fieldWithPath("data.items").type(JsonFieldType.ARRAY).description("태그 목록"),
					fieldWithPath("data.items[].id").type(JsonFieldType.NUMBER).description("태그 ID"),
					fieldWithPath("data.items[].name").type(JsonFieldType.STRING).description("태그 이름"),
					fieldWithPath("error").type(JsonFieldType.NULL).optional().ignored())));
	}

	@Test
	void 연관된_태그_조회() {
		// given
		BDDMockito.given(tagService.getRelatedTags(any(), anyList(), any()))
			.willReturn(new CursorResponse<>(false, 1L, List.of(
				new TagResponse(1L, "relatedTag")
			)));

		// when & then
		given().contentType(ContentType.JSON)
			.param("page", 0)
			.param("size", 10)
			.param("tagNames", List.of("tag1", "tag2"))
			.when().get("/v1/tags/related")
			.then().status(HttpStatus.OK)
			.apply(document("getTags", requestPreprocessor(), responsePreprocessor(),
				queryParameters(
					parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
					parameterWithName("size").description("페이지 크기 (기본값: 10)").optional(),
					parameterWithName("tagNames").description("검색에 사용되는 태그 이름 목록")
				),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"),
					fieldWithPath("data.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 여부"),
					fieldWithPath("data.lastCursor").type(JsonFieldType.NUMBER).description("마지막 커서 ID"),
					fieldWithPath("data.items").type(JsonFieldType.ARRAY).description("연관된 태그 목록"),
					fieldWithPath("data.items[].id").type(JsonFieldType.NUMBER).description("태그 ID"),
					fieldWithPath("data.items[].name").type(JsonFieldType.STRING).description("태그 이름"),
					fieldWithPath("error").type(JsonFieldType.NULL).optional().ignored())));
	}
}
