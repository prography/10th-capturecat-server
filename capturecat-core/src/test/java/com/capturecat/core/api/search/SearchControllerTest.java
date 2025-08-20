package com.capturecat.core.api.search;

import static com.capturecat.test.api.RestDocsUtil.requestPreprocessor;
import static com.capturecat.test.api.RestDocsUtil.responsePreprocessor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.capturecat.core.service.search.SearchService;
import com.capturecat.test.api.RestDocsTest;

class SearchControllerTest extends RestDocsTest {

	private SearchService searchService;
	private SearchController searchController;

	@BeforeEach
	void setUp() {
		searchService = mock(SearchService.class);
		searchController = new SearchController(searchService);
		mockMvc = mockController(searchController);
	}

	@Test
	void 검색어_자동완성() {
		// given
		BDDMockito.given(searchService.autocomplete(any(), anyString(), anyInt())).willReturn(List.of(
			new TagResponse(1L, "자바"),
			new TagResponse(2L, "자바스크립트")
		));

		// when & then
		given().contentType(ContentType.JSON).log().all()
			.accept(ContentType.JSON)
			.queryParam("keyword", "자")
			.queryParam("size", 10)
			.when().get("/v1/search/autocomplete")
			.then().status(HttpStatus.OK)
			.apply(document("autocomplete", requestPreprocessor(), responsePreprocessor(),
				queryParameters(
					parameterWithName("keyword").description("검색어"),
					parameterWithName("size").description("결과 개수, 기본값은 10")
				),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 성공 여부"),
					fieldWithPath("data").type(JsonFieldType.ARRAY).description("자동 완성 결과"),
					fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("태그 ID"),
					fieldWithPath("data[].name").type(JsonFieldType.STRING).description("태그 이름")
				)));
	}
}
