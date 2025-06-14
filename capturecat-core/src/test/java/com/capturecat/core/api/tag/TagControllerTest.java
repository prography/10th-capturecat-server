package com.capturecat.core.api.tag;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import com.capturecat.core.service.tag.TagService;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;
import com.capturecat.core.support.handler.CoreExceptionHandler;
import com.capturecat.test.api.RestDocsTest;

import io.restassured.http.ContentType;

import static com.capturecat.test.api.RestDocsUtil.requestPreprocessor;
import static com.capturecat.test.api.RestDocsUtil.responsePreprocessor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

class TagControllerTest extends RestDocsTest {

    private TagService tagService;

    private TagController tagController;

    private CoreExceptionHandler coreExceptionHandler = new CoreExceptionHandler();

    @BeforeEach
    void setUp() {
        tagService = mock(TagService.class);
        tagController = new TagController(tagService);
        mockMvc = mockController(tagController, coreExceptionHandler);
    }

    @Test
    void 단일_이미지에_태그를_등록한다() {
        // given
        AddTagsToImageRequest request = new AddTagsToImageRequest(List.of("tag1", "tag2"));
        Long imageId = 1L;
        willDoNothing().given(tagService).addTagsToImage(anyLong(), any());


        // when & then
        given().contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/images/{imageId}/tags", imageId)
                .then()
                .status(HttpStatus.OK)
                .apply(document("addTagsToImage", requestPreprocessor(), responsePreprocessor(),
                        pathParameters(
                                parameterWithName("imageId").description("태그를 등록할 이미지 ID")
                        ),
                        requestFields(
                                fieldWithPath("tags").type(JsonFieldType.ARRAY).description("등록할 태그 목록")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"),
                                fieldWithPath("data").type(JsonFieldType.NULL).ignored(),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).ignored()
                        )));
    }

    @Test
    void 단일_이미지에_태그를_등록_시_이미지가_존재하지_않으면_예외가_발생한다() {
        // given
        AddTagsToImageRequest request = new AddTagsToImageRequest(List.of("tag1", "tag2"));
        Long imageId = 1L;
        willThrow(new CoreException(ErrorType.IMAGE_NOT_FOUND)).given(tagService).addTagsToImage(anyLong(), any());


        // when & then
        given().contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/images/{imageId}/tags", imageId)
                .then()
                .status(HttpStatus.NOT_FOUND)
                .apply(document("addTagsToImage", requestPreprocessor(), responsePreprocessor(),
                        pathParameters(
                                parameterWithName("imageId").description("태그를 등록할 이미지 ID")
                        ),
                        requestFields(
                                fieldWithPath("tags").type(JsonFieldType.ARRAY).description("등록할 태그 목록")
                        ),
                        responseFields(
                                fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"),
                                fieldWithPath("data").type(JsonFieldType.NULL).ignored(),
                                fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
                                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지")
                        )));
    }
}
