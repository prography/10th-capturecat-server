package com.capturecat.core.api.image;

import static com.capturecat.test.api.RestDocsUtil.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.http.ContentType;

import com.capturecat.core.DummyObject;
import com.capturecat.core.api.image.dto.AddTagsToImageRequest;
import com.capturecat.core.api.image.dto.ImageMapper;
import com.capturecat.core.api.image.dto.RemoveTagsToImageRequest;
import com.capturecat.core.domain.image.Image;
import com.capturecat.core.service.image.ImageService;
import com.capturecat.test.api.RestDocsTest;

class ImageControllerTest extends RestDocsTest {

	private static final String URL_PREFIX = "/v1/images";

	private final ImageMapper mapper = new ImageMapper(new ModelMapper());
	private final ObjectMapper objectMapper = new ObjectMapper();

	private ImageService imageService;

	private ImageController imageController;

	@BeforeEach
	void setUp() {
		imageService = mock(ImageService.class);
		imageController = new ImageController(imageService);
		mockMvc = mockController(imageController);
	}

	@Test
	void 이미지업로드_성공() {
		// given
		List<Image> images = DummyObject.newMockImages(1, 2);
		when(imageService.save(any())).thenReturn(mapper.toDto(images));

		// when & then
		given().contentType(MediaType.MULTIPART_FORM_DATA)
			.multiPart("files", "cat.jpg", "file-content-1".getBytes())
			.multiPart("files", "dog.jpg", "file-content-2".getBytes())
			.when().post(URL_PREFIX + "/upload")
			.then().status(HttpStatus.OK)
			.apply(document("upload", requestPreprocessor(), responsePreprocessor(),
				requestParts(partWithName("files").description("업로드할 이미지 파일들")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 성공 여부"),
					fieldWithPath("data").type(JsonFieldType.OBJECT).description("DB에 저장된 이미지 정보"),
					fieldWithPath("data.images").type(JsonFieldType.ARRAY).description("업로드된 이미지 목록"),
					fieldWithPath("data.images[].id").type(JsonFieldType.NUMBER).description("이미지 ID"),
					fieldWithPath("data.images[].fileName").type(JsonFieldType.STRING).description("파일 이름"),
					fieldWithPath("data.images[].fileUrl").type(JsonFieldType.STRING).description("파일 URL"),
					fieldWithPath("data.images[].size").type(JsonFieldType.NUMBER).description("파일 크기"),
					fieldWithPath("data.images[].createdDate").type(JsonFieldType.STRING).description("이미지 생성 일시"),
					fieldWithPath("data.images[].lastModifiedDate").type(JsonFieldType.STRING)
						.description("이미지 최종 수정 일시"),
					fieldWithPath("error").type(JsonFieldType.NULL).optional().ignored())));
	}

	@Test
	void 공통_태그를_등록한다() throws JsonProcessingException {
		// given
		AddTagsToImageRequest request = new AddTagsToImageRequest(List.of("tag1", "tag2"));
		willDoNothing().given(imageService).addCommonTags(anyList(), any());

		// when & then
		given().contentType(ContentType.MULTIPART)
			.accept(ContentType.JSON)
			.log().all()
			.multiPart("files", "cat.jpg", "file-content-1".getBytes())
			.multiPart("files", "dog.jpg", "file-content-2".getBytes())
			.multiPart("request",  "request.json", objectMapper.writeValueAsString(request),
				MediaType.APPLICATION_JSON_VALUE)
			.when().post(URL_PREFIX + "/common-tags")
			.then().status(HttpStatus.OK)
			.log().all()
			.apply(document("addCommonTags", requestPreprocessor(), responsePreprocessor(),
				requestParts(
					partWithName("files").description("업로드할 이미지 파일들"),
					partWithName("request").description("공통 태그 등록 요청")),
				requestPartFields("request",
					fieldWithPath("tagNames").type(JsonFieldType.ARRAY).description("태그 이름 목록")
				),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"),
					fieldWithPath("data").type(JsonFieldType.NULL).ignored(),
					fieldWithPath("error").type(JsonFieldType.OBJECT).ignored())));
	}

	@Test
	void 단일_이미지에_태그를_등록한다() {
		// given
		AddTagsToImageRequest request = new AddTagsToImageRequest(List.of("tag1", "tag2"));
		Long imageId = 1L;
		willDoNothing().given(imageService).addTagsToImage(anyLong(), any());

		// when & then
		given().contentType(ContentType.JSON)
			.body(request)
			.when().post(URL_PREFIX + "/{imageId}/tags", imageId)
			.then().status(HttpStatus.OK)
			.apply(document("addTagsToImage", requestPreprocessor(), responsePreprocessor(),
				pathParameters(parameterWithName("imageId").description("태그를 등록할 이미지 ID")),
				requestFields(fieldWithPath("tagNames").type(JsonFieldType.ARRAY).description("등록할 태그 목록")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"),
					fieldWithPath("data").type(JsonFieldType.NULL).ignored(),
					fieldWithPath("error").type(JsonFieldType.OBJECT).ignored())));
	}

	@Test
	void 태그를_삭제한다() {
		// given
		Long imageId = 1L;
		RemoveTagsToImageRequest request = new RemoveTagsToImageRequest(List.of(1L, 2L));
		willDoNothing().given(imageService).removeTagsToImage(anyLong(), anyList());

		// when & then
		given().contentType(ContentType.JSON)
			.body(request)
			.when().delete(URL_PREFIX + "/{imageId}/tags", imageId)
			.then().status(HttpStatus.OK)
			.apply(document("removeTagsToImage", requestPreprocessor(), responsePreprocessor(),
				pathParameters(parameterWithName("imageId").description("태그를 삭제할 이미지 ID")),
				requestFields(fieldWithPath("tagIds").type(JsonFieldType.ARRAY).description("삭제할 태그 ID 목록")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"),
					fieldWithPath("data").type(JsonFieldType.NULL).ignored(),
					fieldWithPath("error").type(JsonFieldType.OBJECT).ignored())));
	}

}
