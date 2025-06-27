package com.capturecat.core.api.image;

import static com.capturecat.test.api.RestDocsUtil.requestPreprocessor;
import static com.capturecat.test.api.RestDocsUtil.responsePreprocessor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.http.ContentType;

import com.capturecat.core.api.image.dto.AddTagsToImageRequest;
import com.capturecat.core.api.image.dto.RemoveTagsToImageRequest;
import com.capturecat.core.api.image.dto.UploadItemRequest;
import com.capturecat.core.service.image.ImageService;
import com.capturecat.test.api.RestDocsTest;

class ImageControllerTest extends RestDocsTest {

	private static final String URL_PREFIX = "/v1/images";

	private final ObjectMapper om = new ObjectMapper();

	private ImageService imageService;

	private ImageController imageController;

	@BeforeEach
	void setUp() {
		imageService = mock(ImageService.class);
		imageController = new ImageController(imageService);
		mockMvc = mockController(imageController);
	}

	@Test
	void 이미지_업로드_후_태그를_생성한다() throws JsonProcessingException {
		// given
		willDoNothing().given(imageService).save(anyList(), anyList());

		List<UploadItemRequest> requests = List.of(
			new UploadItemRequest("cat.jpg", List.of("고양이", "cat")),
			new UploadItemRequest("dog.jpg", List.of("강아지", "dog"))
		);

		// when & then
		given().contentType(ContentType.MULTIPART).log().all()
			.accept(ContentType.JSON)
			.multiPart("uploadItems", om.writeValueAsString(requests), MediaType.APPLICATION_JSON_VALUE)
			.multiPart("files", "cat.jpg", "file-content-1".getBytes(), MediaType.IMAGE_JPEG_VALUE)
			.multiPart("files", "dog.jpg", "file-content-2".getBytes(), MediaType.IMAGE_JPEG_VALUE)
			.when().post(URL_PREFIX + "/upload")
			.then().status(HttpStatus.OK)
			.apply(document("upload", requestPreprocessor(), responsePreprocessor(),
				requestParts(
					partWithName("files").description("업로드할 이미지 파일들"),
					partWithName("uploadItems").description("업로드할 이미지와 태그 정보")),
				requestPartFields("uploadItems",
					fieldWithPath("[].fileName").description("이미지 파일 이름"),
					fieldWithPath("[].tagNames").description("이미지에 등록할 태그 목록")
				),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 성공 여부"),
					fieldWithPath("data").type(JsonFieldType.NULL).optional().ignored(),
					fieldWithPath("error").type(JsonFieldType.NULL).optional().ignored())));
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
