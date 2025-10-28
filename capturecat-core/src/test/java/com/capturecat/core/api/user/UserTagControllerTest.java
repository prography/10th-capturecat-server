package com.capturecat.core.api.user;

import static com.capturecat.test.api.RestDocsUtil.requestPreprocessor;
import static com.capturecat.test.api.RestDocsUtil.responsePreprocessor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import io.restassured.http.ContentType;

import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.service.image.TagResponse;
import com.capturecat.core.service.tag.ImageTagService;
import com.capturecat.core.service.user.UserTagService;
import com.capturecat.core.support.response.CursorResponse;
import com.capturecat.test.api.RestDocsTest;

class UserTagControllerTest extends RestDocsTest {

	private static final String ACCESS_TOKEN = "valid-access-token";

	private UserTagController userTagController;
	private UserTagService userTagService;
	private ImageTagService imageTagService;

	@BeforeEach
	void setUp() {
		userTagService = mock(UserTagService.class);
		imageTagService = mock(ImageTagService.class);
		userTagController = new UserTagController(userTagService, imageTagService);
		mockMvc = mockController(userTagController);
	}

	@Test
	void 유저_태그_생성() {
		// given
		BDDMockito.given(userTagService.create(any(), anyString())).willReturn(new TagResponse(1L, "java"));

		// when & then
		given()
			.header(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + ACCESS_TOKEN)
			.contentType(ContentType.JSON)
			.queryParam("tagName", "java")
			.when().post("/v1/user-tags")
			.then().status(HttpStatus.OK)
			.apply(document("createUserTag", requestPreprocessor(), responsePreprocessor(),
				requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("유효한 Access 토큰")),
				queryParameters(parameterWithName("tagName").description("태그 이름")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"),
					fieldWithPath("data").type(JsonFieldType.OBJECT).description("사용자가 등록한 태그 정보"),
					fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("태그 ID"),
					fieldWithPath("data.name").type(JsonFieldType.STRING).description("태그 이름"))));
	}

	@Test
	void 유저_태그_조회() {
		// given
		BDDMockito.given(userTagService.getAll(any(), any())).willReturn(
			new CursorResponse(false, 1L, List.of(new TagResponse(1L, "java"))));

		// when & then
		given()
			.header(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + ACCESS_TOKEN)
			.contentType(ContentType.JSON)
			.when().get("/v1/user-tags")
			.then().status(HttpStatus.OK)
			.apply(document("getUserTags", requestPreprocessor(), responsePreprocessor(),
				requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("유효한 Access 토큰")),
				queryParameters(
					parameterWithName("page").description("페이지 번호").optional(),
					parameterWithName("size").description("페이지 크기").optional()
				),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"),
					fieldWithPath("data").type(JsonFieldType.OBJECT).description("커서 페이지 응답"),
					fieldWithPath("data.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("data.lastCursor").type(JsonFieldType.NUMBER).description("마지막 커서 ID"),
					fieldWithPath("data.items").type(JsonFieldType.ARRAY).description("태그 목록"),
					fieldWithPath("data.items[].id").type(JsonFieldType.NUMBER).description("태그 ID"),
					fieldWithPath("data.items[].name").type(JsonFieldType.STRING).description("태그 이름"))));
	}

	@Test
	void 유저_태그_수정() {
		// given
		BDDMockito.given(userTagService.update(any(), anyLong(), anyString())).willReturn(new TagResponse(1L, "java"));
		BDDMockito.willDoNothing().given(imageTagService).update(any(), anyLong(), anyLong());

		// when & then
		given()
			.header(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + ACCESS_TOKEN)
			.contentType(ContentType.JSON)
			.body(Map.of("currentTagId", 1L, "newTagName", "spring"))
			.when().patch("/v1/user-tags")
			.then().status(HttpStatus.OK)
			.apply(document("updateUserTag", requestPreprocessor(), responsePreprocessor(),
				requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("유효한 Access 토큰")),
				requestFields(
					fieldWithPath("currentTagId").type(JsonFieldType.NUMBER).description("수정할 태그 ID"),
					fieldWithPath("newTagName").type(JsonFieldType.STRING).description("새로운 태그 이름")
				),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"),
					fieldWithPath("data").type(JsonFieldType.OBJECT).description("수정된 태그 정보"),
					fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("태그 ID"),
					fieldWithPath("data.name").type(JsonFieldType.STRING).description("태그 이름"))));
	}

	@Test
	void 유저_태그_삭제() {
		// given
		BDDMockito.given(userTagService.create(any(), anyString())).willReturn(new TagResponse(1L, "java"));
		BDDMockito.willDoNothing().given(imageTagService).delete(any(), anyLong());

		// when & then
		given()
			.header(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + ACCESS_TOKEN)
			.contentType(ContentType.JSON)
			.queryParam("tagId", 1L)
			.when().delete("/v1/user-tags")
			.then().status(HttpStatus.OK)
			.apply(document("deleteUserTag", requestPreprocessor(), responsePreprocessor(),
				requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("유효한 Access 토큰")),
				queryParameters(parameterWithName("tagId").description("태그 ID")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"))));
	}
}
