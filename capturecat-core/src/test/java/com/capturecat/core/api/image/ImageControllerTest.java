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
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.http.ContentType;

import com.capturecat.core.api.image.dto.AddTagsToImageRequest;
import com.capturecat.core.api.image.dto.UploadItemRequest;
import com.capturecat.core.service.image.ImageService;
import com.capturecat.core.service.image.ImageWithTagsResponse;
import com.capturecat.core.service.image.TagResponse;
import com.capturecat.core.support.response.CursorResponse;
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
		willDoNothing().given(imageService).save(anyList(), anyList(), any());

		List<UploadItemRequest> requests = List.of(
			new UploadItemRequest("cat.jpg", LocalDate.now().toString(), true, List.of("고양이", "cat")),
			new UploadItemRequest("dog.jpg", LocalDate.now().toString(), false, List.of("강아지", "dog"))
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
					fieldWithPath("[].captureDate").description("이미지를 캡처한 날짜"),
					fieldWithPath("[].isBookmarked").description("이미지를 즐겨찾기 여부"),
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
		willDoNothing().given(imageService).addTagsToImage(anyLong(), any(), any());

		// when & then
		given().contentType(ContentType.JSON)
			.body(request)
			.when().post(URL_PREFIX + "/{imageId}/tags", imageId)
			.then().status(HttpStatus.OK)
			.apply(document("addTagsToImage", requestPreprocessor(), responsePreprocessor(),
				pathParameters(parameterWithName("imageId").description("태그를 등록할 이미지 ID")),
				requestFields(fieldWithPath("tagNames").type(JsonFieldType.ARRAY).description("등록할 태그 목록")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"))));
	}

	@Test
	void 태그와_이미지를_조회한다() {
		// given
		BDDMockito.given(imageService.getImagesWithTags(any(), any(), any(Pageable.class)))
			.willReturn(new CursorResponse<>(false, 1L,
				List.of(new ImageWithTagsResponse(1L, "cat.jpg", "http://example.com/cat.jpg", LocalDate.now(), true,
					List.of(new TagResponse(1L, "고양이"), new TagResponse(2L, "cat"))))));

		// when & then
		given().contentType(ContentType.JSON)
			.param("page", 0)
			.param("size", 20)
			.when().get(URL_PREFIX)
			.then().status(HttpStatus.OK).log().all()
			.apply(document("getImagesWithTagsByUser", requestPreprocessor(), responsePreprocessor(),
				queryParameters(
					parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
					parameterWithName("size").description("페이지 크기 (기본값: 20, 최대: 100)").optional(),
					parameterWithName("hasTags").description(
						"이미지 태그 존재 유무로 필터링합니다.\n\n"
							+ "- `true`: 태그가 있는 이미지만 조회\n\n"
							+ "- `false`: 태그가 없는 이미지만 조회\n\n"
							+ "- 미입력 시: 전체 이미지 조회").optional()
				),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"),
					fieldWithPath("data.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 여부"),
					fieldWithPath("data.lastCursor").type(JsonFieldType.NUMBER).description("마지막 커서 ID"),
					fieldWithPath("data.items").type(JsonFieldType.ARRAY).description("이미지 및 태그 목록"),
					fieldWithPath("data.items[].id").type(JsonFieldType.NUMBER).description("이미지 ID"),
					fieldWithPath("data.items[].name").type(JsonFieldType.STRING).description("이미지 이름"),
					fieldWithPath("data.items[].url").type(JsonFieldType.STRING).description("이미지 URL"),
					fieldWithPath("data.items[].captureDate").type(JsonFieldType.STRING).description("캡처한 날짜"),
					fieldWithPath("data.items[].isBookmarked").type(JsonFieldType.BOOLEAN).description("즐겨찾기 여부"),
					fieldWithPath("data.items[].tags").type(JsonFieldType.ARRAY).description("이미지에 등록된 태그 목록"),
					fieldWithPath("data.items[].tags[].id").type(JsonFieldType.NUMBER).description("태그 ID"),
					fieldWithPath("data.items[].tags[].name").type(JsonFieldType.STRING).description("태그 이름"),
					fieldWithPath("error").type(JsonFieldType.NULL).optional().ignored())));
	}

	@Test
	void 태그로_이미지를_검색한다() {
		// given
		List<String> tagNames = List.of("tag1", "tag2");

		BDDMockito.given(imageService.searchImagesByTagNames(any(), any(), any(Pageable.class)))
			.willReturn(new CursorResponse<>(false, 1L,
				List.of(new ImageWithTagsResponse(1L, "cat.jpg", "http://example.com/cat.jpg", LocalDate.now(), false,
					List.of(new TagResponse(1L, "고양이"), new TagResponse(2L, "cat"))))));

		// when & then
		given().contentType(ContentType.JSON)
			.param("tagNames", tagNames)
			.param("page", 0)
			.param("size", 20)
			.when().get(URL_PREFIX + "/search")
			.then().status(HttpStatus.OK)
			.apply(document("searchImagesByTag", requestPreprocessor(), responsePreprocessor(),
				queryParameters(
					parameterWithName("tagNames").description("검색하는 태그 리스트").optional(),
					parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
					parameterWithName("size").description("페이지 크기 (기본값: 20, 최대: 100)").optional()),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"),
					fieldWithPath("data.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 여부"),
					fieldWithPath("data.lastCursor").type(JsonFieldType.NUMBER).description("마지막 커서 ID"),
					fieldWithPath("data.items").type(JsonFieldType.ARRAY).description("이미지 및 태그 목록"),
					fieldWithPath("data.items[].id").type(JsonFieldType.NUMBER).description("이미지 ID"),
					fieldWithPath("data.items[].name").type(JsonFieldType.STRING).description("이미지 이름"),
					fieldWithPath("data.items[].url").type(JsonFieldType.STRING).description("이미지 URL"),
					fieldWithPath("data.items[].captureDate").type(JsonFieldType.STRING).description("캡처한 날짜"),
					fieldWithPath("data.items[].isBookmarked").type(JsonFieldType.BOOLEAN).description("즐겨찾기 여부"),
					fieldWithPath("data.items[].tags").type(JsonFieldType.ARRAY).description("이미지에 등록된 태그 목록"),
					fieldWithPath("data.items[].tags[].id").type(JsonFieldType.NUMBER).description("태그 ID"),
					fieldWithPath("data.items[].tags[].name").type(JsonFieldType.STRING).description("태그 이름"),
					fieldWithPath("error").type(JsonFieldType.NULL).optional().ignored())));
	}

	@Test
	void 단건_이미지를_조회한다() {
		// given
		Long imageId = 1L;

		BDDMockito.given(imageService.getImageWithTags(anyLong(), any()))
			.willReturn(new ImageWithTagsResponse(1L, "cat.jpg", "http://example.com/cat.jpg", LocalDate.now(), true,
				List.of(new TagResponse(1L, "고양이"), new TagResponse(2L, "cat"))));

		// when & then
		given().contentType(ContentType.JSON)
			.when().get(URL_PREFIX + "/{imageId}", imageId)
			.then().status(HttpStatus.OK)
			.apply(document("getImageWithTags", requestPreprocessor(), responsePreprocessor(),
				pathParameters(parameterWithName("imageId").description("조회할 이미지 ID")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"),
					fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("이미지 ID"),
					fieldWithPath("data.name").type(JsonFieldType.STRING).description("이미지 이름"),
					fieldWithPath("data.url").type(JsonFieldType.STRING).description("이미지 URL"),
					fieldWithPath("data.captureDate").type(JsonFieldType.STRING).description("캡처한 날짜"),
					fieldWithPath("data.isBookmarked").type(JsonFieldType.BOOLEAN).description("즐겨찾기 여부"),
					fieldWithPath("data.tags").type(JsonFieldType.ARRAY).description("이미지에 등록된 태그 목록"),
					fieldWithPath("data.tags[].id").type(JsonFieldType.NUMBER).description("태그 ID"),
					fieldWithPath("data.tags[].name").type(JsonFieldType.STRING).description("태그 이름"),
					fieldWithPath("error").type(JsonFieldType.NULL).optional().ignored())));
	}

	@Test
	void 이미지를_삭제한다() {
		// given
		Long imageId = 1L;
		willDoNothing().given(imageService).removeImages(anyLong(), any());

		// when & then
		given().contentType(ContentType.JSON)
			.when().delete(URL_PREFIX + "/{imageId}", imageId)
			.then().status(HttpStatus.OK)
			.apply(document("removeImage", requestPreprocessor(), responsePreprocessor(),
				pathParameters(parameterWithName("imageId").description("삭제할 이미지 ID")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"))));
	}

	@Test
	void 태그를_삭제한다() {
		// given
		Long imageId = 1L;
		Long tagId = 1L;
		willDoNothing().given(imageService).removeTagToImage(anyLong(), anyLong(), any());

		// when & then
		given().contentType(ContentType.JSON)
			.when().delete(URL_PREFIX + "/{imageId}/tags/{tagId}", imageId, tagId)
			.then().status(HttpStatus.OK)
			.apply(document("removeTagToImage", requestPreprocessor(), responsePreprocessor(),
				pathParameters(
					parameterWithName("imageId").description("이미지 ID"),
					parameterWithName("tagId").description("삭제할 이미지 ID")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"))));
	}
}
