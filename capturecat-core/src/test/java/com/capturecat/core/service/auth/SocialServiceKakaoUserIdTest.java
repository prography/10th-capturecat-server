package com.capturecat.core.service.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import com.capturecat.core.config.auth.Oauth2Properties;
import com.capturecat.core.config.auth.SocialApiProperties;
import com.capturecat.core.support.error.CoreException;

public class SocialServiceKakaoUserIdTest {
	private SocialService service;
	private WebClient webClient;
	private Oauth2Properties oauth2Properties;
	private SocialApiProperties socialApiProperties;

	@BeforeEach
	void setUp() {
		oauth2Properties = new Oauth2Properties();

		socialApiProperties = new SocialApiProperties();
		SocialApiProperties.Kakao kakao = new SocialApiProperties.Kakao();
		kakao.setUserinfoUrl("https://kapi.kakao.com/v2/user/me");
		socialApiProperties.setKakao(kakao);

		webClient = Mockito.mock(WebClient.class);
		service = Mockito.spy(new SocialService(webClient, oauth2Properties, socialApiProperties));
	}

	@DisplayName("정상 accessToken -> userId 추출")
	@Test
	void fetchKakaoUserId_success() {
		// WebClient 체인 mock 준비
		WebClient.ResponseSpec responseSpec = getResponseSpec();

		// 카카오 API 응답 mock
		Map<String, Object> fakeResponse = Map.of("id", 123456789L);
		Mockito.when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(fakeResponse));

		// when
		String userId = service.fetchKakaoUserId("dummyAccessToken");

		// then
		assertThat(userId).isEqualTo("123456789");
	}

	@DisplayName("id 누락시 예외 발생")
	@Test
	void fetchKakaoUserId_fail_noId() {
		WebClient.ResponseSpec responseSpec = getResponseSpec();

		Map<String, Object> fakeResponse = Map.of(); // id 없음
		Mockito.when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(fakeResponse));

		assertThatThrownBy(() -> service.fetchKakaoUserId("dummyAccessToken"))
			.isInstanceOf(CoreException.class);
	}

	private WebClient.ResponseSpec getResponseSpec() {
		// WebClient 체인 mock 준비
		WebClient.RequestBodyUriSpec uriSpec = Mockito.mock(WebClient.RequestBodyUriSpec.class);
		WebClient.RequestBodySpec bodySpec = Mockito.mock(WebClient.RequestBodySpec.class);
		WebClient.RequestHeadersSpec headersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
		WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

		// 2. Mock 체인 연결
		Mockito.when(webClient.post()).thenReturn(uriSpec);
		Mockito.when(uriSpec.uri(anyString())).thenReturn(bodySpec);
		// 두 번의 .header() 체이닝 모두 bodySpec 반환
		Mockito.when(bodySpec.header(anyString(), anyString())).thenReturn(bodySpec); // 여러 번 호출되어도 OK
		Mockito.when(bodySpec.retrieve()).thenReturn(responseSpec);
		return responseSpec;
	}
}
