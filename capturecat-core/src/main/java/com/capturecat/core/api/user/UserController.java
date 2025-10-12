package com.capturecat.core.api.user;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.api.user.dto.UserReqDto.JoinReqDto;
import com.capturecat.core.api.user.dto.UserReqDto.UserSettingsReqDto;
import com.capturecat.core.api.user.dto.UserReqDto.WithdrawReqDto;
import com.capturecat.core.api.user.dto.UserRespDto.InfoRespDto;
import com.capturecat.core.api.user.dto.UserRespDto.JoinRespDto;
import com.capturecat.core.api.user.dto.UserRespDto.UserSettingsRespDto;
import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.domain.user.UserSettings;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.auth.TokenService;
import com.capturecat.core.service.user.UserService;
import com.capturecat.core.support.response.ApiResponse;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final TokenService tokenService;

	/**
	 * 일반 회원 가입 (소셜 로그인 x)
	 */
	@PostMapping("/join")
	public ApiResponse<JoinRespDto> join(@RequestBody @Valid JoinReqDto joinReqDto, BindingResult bindingResult) {
		JoinRespDto joinRespDto = userService.join(joinReqDto);

		return ApiResponse.success(joinRespDto);
	}

	/**
	 * 튜토리얼(시작하기) 완료 업데이트
	 */
	@PostMapping("/tutorialComplete")
	public ApiResponse<?> tutorialCompleted(@AuthenticationPrincipal LoginUser loginUser) {
		userService.updateTutorialCompleted(loginUser);
		return ApiResponse.success();
	}

	/**
	 * 탈퇴 API
	 * 1) 소셜 로그인 연결 해제
	 * 2) 탈퇴 사유 저장 - 실패해도 1,2 롤백 X (별도 TX)
	 * 3) 회원 및 관련 데이터 삭제
	 */
	@DeleteMapping("/withdraw")
	public ApiResponse<?> withdraw(@AuthenticationPrincipal LoginUser loginUser, @RequestHeader HttpHeaders headers,
		@RequestBody @Valid WithdrawReqDto reqDto, BindingResult bindingResult) {
		//1. 소셜 계정 연동 해제 및 회원 정보 삭제
		String resultMessage = userService.withdraw(loginUser, reqDto.getReason());

		//2. Refresh Token 삭제 및 Access Token 블랙리스트 등록
		String accessHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
		String refreshHeader = headers.getFirst(JwtUtil.REFRESH_TOKEN_HEADER);
		tokenService.revokeUserTokens(accessHeader, refreshHeader);

		return ApiResponse.success(resultMessage);
	}

	/**
	 * 회원 정보 조회
	 */
	@GetMapping("/info")
	public ApiResponse<InfoRespDto> getUserInfo(@AuthenticationPrincipal LoginUser loginUser) {
		InfoRespDto infoRespDto = userService.getUserInfo(loginUser.getUsername());

		return ApiResponse.success(infoRespDto);
	}

	/**
	 * 회원 설정 정보 조회
	 */
	@GetMapping("/settings")
	public ApiResponse<UserSettingsRespDto> getUserSettings(@AuthenticationPrincipal LoginUser loginUser) {
		UserSettings settings = userService.getUserSettings(loginUser.getUsername());

		return ApiResponse.success(new UserSettingsRespDto(settings));
	}

	/**
	 * 회원 설정 정보 변경
	 */
	@PutMapping("/settings")
	public ApiResponse<UserSettingsRespDto> updateUserSettings(@AuthenticationPrincipal LoginUser loginUser,
		UserSettingsReqDto userSettingsReqDto) {
		UserSettings settings = userService.setUserSettings(loginUser.getUsername(),
			userSettingsReqDto.isScreenshotAutoDeleteEnabled());

		return ApiResponse.success(new UserSettingsRespDto(settings));
	}
}
