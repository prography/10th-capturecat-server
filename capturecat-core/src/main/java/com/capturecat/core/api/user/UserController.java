package com.capturecat.core.api.user;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.api.user.dto.UserReqDto.JoinReqDto;
import com.capturecat.core.api.user.dto.UserReqDto.JoinRespDto;
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
	 */
	@DeleteMapping("/withdraw")
	public ApiResponse<?> withdraw(@AuthenticationPrincipal LoginUser loginUser,
		@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
		//1. 소셜 계정 연동 해제 및 회원 정보 삭제
		String resultMessage = userService.withdraw(loginUser);

		//2. Refresh Token 삭제
		tokenService.deleteRefreshTokenByUsername(loginUser.getUsername());

		//3. Access Token 블랙리스트 등록
		tokenService.blacklistAccessToken(authHeader);

		return ApiResponse.success(resultMessage);
	}
}
