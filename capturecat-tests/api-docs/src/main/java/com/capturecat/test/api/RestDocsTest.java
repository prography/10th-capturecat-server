package com.capturecat.test.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;

/**
 * Spring Rest Docs 테스트를 위한 공통 기반 클래스입니다.
 * 이 클래스를 상속받는 모든 테스트는 Rest Docs 기능을 사용하게 됩니다.
 */
@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsTest {

    protected MockMvcRequestSpecification mockMvc;

    private RestDocumentationContextProvider restDocumentation;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.restDocumentation = restDocumentation;
    }

    /**
     * RestAssuredMockMvc의 요청 스펙 반환
     * 테스트 코드에서 given() 메서드를 통해 요청 빌더를 얻어 사용할 수 있음
     *
     * @return MockMvcRequestSpecification - MockMvc 기반 요청 스펙
     */
    protected MockMvcRequestSpecification given() {
        return mockMvc;
    }

    /**
     * 주어진 컨트롤러 및 컨트롤러 어드바이스를 기반으로 MockMvc와 RestAssuredMockMvc 요청 스펙을 초기화합니다.
     * 이를 통해 테스트 대상 컨트롤러에 대해 API 요청 테스트와 Spring REST Docs 문서 생성을 함께 수행할 수 있습니다.
     *
     * @param controller 테스트 대상 컨트롤러 인스턴스
     * @param controllerAdvice 예외 처리 등을 위한 @ControllerAdvice 인스턴스
     * @return 요청 테스트 및 문서화를 위한 MockMvcRequestSpecification 객체
     */
    protected MockMvcRequestSpecification mockController(Object controller, Object controllerAdvice) {
        MockMvc mockMvc = createMockMvc(controller, controllerAdvice);
        return RestAssuredMockMvc.given().mockMvc(mockMvc);
    }

    /**
     * 주어진 컨트롤러 및 컨트롤러 어드바이스를 설정하여 MockMvc 인스턴스를 생성합니다.
     * - Jackson 메시지 컨버터를 통해 JSON 직렬화/역직렬화 설정을 커스터마이징하고,
     * - REST Docs 문서화 설정을 함께 적용합니다.
     *
     * @param controller 테스트 대상 컨트롤러 인스턴스
     * @param controllerAdvice 예외 처리 등을 위한 @ControllerAdvice 인스턴스
     * @return 문서화 및 테스트에 사용할 MockMvc 객체
     */
    private MockMvc createMockMvc(Object controller, Object controllerAdvice) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper());

        return MockMvcBuilders.standaloneSetup(controller)
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .setMessageConverters(converter)
                .setControllerAdvice(controllerAdvice)
                .build();
    }

    /**
     * Jackson ObjectMapper를 생성하여 날짜와 기간 관련 직렬화 방식을 타임스탬프 대신 ISO 포맷으로 설정함
     * 모듈 자동 등록 기능도 활성화되어 있음
     *
     * @return ObjectMapper - 커스터마이징된 JSON ObjectMapper 인스턴스
     */
    private ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);
    }
}
