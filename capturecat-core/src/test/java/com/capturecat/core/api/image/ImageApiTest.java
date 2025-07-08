package com.capturecat.core.api.image;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import com.capturecat.core.api.image.dto.AddTagsToImageRequest;
import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.support.error.ErrorCode;
import com.capturecat.core.support.error.ErrorMessage;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ImageApiTest {

	@LocalServerPort
	private int port;

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private ImageTagRepository imageTagRepository;

	private Long imageId;

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
		// 임시 초기화 작업
		imageId = imageRepository.save(Image.builder().build()).getId();
	}

	@Test
	void 단일_이미지_태그를_등록한다() {
		// given
		AddTagsToImageRequest 단일_이미지_태그_등록_요청 = new AddTagsToImageRequest(List.of("tag1", "tag2"));

		// when & then
		RestAssured.given().log().all()
			.contentType(ContentType.JSON)
			.body(단일_이미지_태그_등록_요청)
			.when().post("/v1/images/{imageId}/tags", imageId)
			.then().log().all()
			.statusCode(HttpStatus.OK.value());
	}

	@Test
	void 단일_이미지_태그_등록_시_이미_등록되어_있는_태그가_있는_경우_400을_반환한다() {
		// given
		AddTagsToImageRequest 단일_이미지_태그_등록_요청1 = new AddTagsToImageRequest(List.of("tag1", "tag2"));

		RestAssured.given()
			.contentType(ContentType.JSON)
			.body(단일_이미지_태그_등록_요청1)
			.when().post("/v1/images/{imageId}/tags", imageId)
			.then();

		AddTagsToImageRequest 단일_이미지_태그_등록_요청2 = new AddTagsToImageRequest(List.of("tag1", "tag2", "tag3"));

		// when & then
		RestAssured.given().log().all()
			.contentType(ContentType.JSON)
			.body(단일_이미지_태그_등록_요청2)
			.when().post("/v1/images/{imageId}/tags", imageId)
			.then().log().all()
			.statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	void 단일_이미지_태그_등록_시_태그_개수를_초과하면_400을_반환한다() {
		// given
		AddTagsToImageRequest 단일_이미지_태그_등록_요청 = new AddTagsToImageRequest(
			List.of("tag1", "tag2", "tag3", "tag4", "tag5"));

		// when
		ErrorMessage error = RestAssured.given().log().all()
			.contentType(ContentType.JSON)
			.body(단일_이미지_태그_등록_요청)
			.when().post("/v1/images/{imageId}/tags", imageId)
			.then().log().all()
			.statusCode(HttpStatus.BAD_REQUEST.value())
			.extract().jsonPath().getObject("error", ErrorMessage.class);

		// then
		assertThat(error.code()).isEqualTo(ErrorCode.EXCEED_MAX_TAG_COUNT.name());
		assertThat(error.message()).isEqualTo(ErrorCode.EXCEED_MAX_TAG_COUNT.getMessage());
	}

	@Test
	void 이미지_태그를_삭제한다() {
		// given
		AddTagsToImageRequest 단일_이미지_태그_등록_요청 = new AddTagsToImageRequest(List.of("tag1"));

		RestAssured.given()
			.contentType(ContentType.JSON)
			.body(단일_이미지_태그_등록_요청)
			.when().post("/v1/images/{imageId}/tags", imageId)
			.then();

		// when
		RestAssured.given().log().all()
			.contentType(ContentType.JSON)
			.when().delete("/v1/images/{imageId}/tags/{tagId}", imageId, 1L)
			.then().log().all()
			.statusCode(HttpStatus.OK.value());

		// then
		// TODO: 이미지 조회 및 태그 목록 조회 API 개발 후 재검증
		Image image = imageRepository.findById(imageId).get();
		List<String> tagNamesByImage = imageTagRepository.findTagNamesByImage(image);
		assertThat(tagNamesByImage).isEmpty();
	}
}
