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

import io.restassured.http.ContentType;

import com.capturecat.core.api.image.dto.AddTagsToImageRequest;
import com.capturecat.core.api.image.dto.RemoveTagsToImageRequest;
import com.capturecat.core.service.image.ImageService;
import com.capturecat.test.api.RestDocsTest;

class ImageControllerTest extends RestDocsTest {

	private static final String URL_PREFIX = "/v1/images";

	private ImageService imageService;

	private ImageController imageController;

	@BeforeEach
	void setUp() {
		imageService = mock(ImageService.class);
		imageController = new ImageController(imageService);
		mockMvc = mockController(imageController);
	}

	@Test
	void 이미지_업로드_후_태그를_생성한다() {
		// given
		willDoNothing().given(imageService).save(anyList());

		// when & then
		given().contentType(ContentType.MULTIPART)
			.multiPart("uploadItems[0].file", "cat.jpg", "file-content-1".getBytes(), MediaType.IMAGE_JPEG_VALUE)
			.param("uploadItems[0].tagNames", "고양이", "cat")
			.multiPart("uploadItems[1].file", "dog.jpg", "file-content-2".getBytes(), MediaType.IMAGE_JPEG_VALUE)
			.param("uploadItems[1].tagNames", "강아지", "dog")
			.when().post(URL_PREFIX + "/upload")
			.then().status(HttpStatus.OK)
			.apply(document("upload", requestPreprocessor(), responsePreprocessor(),
				requestParts(
					partWithName("uploadItems[0].file").description("첫 번째 이미지 파일"),
					partWithName("uploadItems[1].file").description("두 번째 이미지 파일")),
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
