package com.capturecat.core.api.user;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.api.user.dto.UserReqDto.JoinReqDto;
import com.capturecat.core.api.user.dto.UserReqDto.JoinRespDto;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.user.UserService;
import com.capturecat.core.support.response.ApiResponse;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/join")
	public ApiResponse<JoinRespDto> join(@RequestBody @Valid JoinReqDto joinReqDto, BindingResult bindingResult) {
		JoinRespDto joinRespDto = userService.join(joinReqDto);
		return ApiResponse.success(joinRespDto);
	}

	@PostMapping("/tutorialComplete")
	public ApiResponse<?> tutorialCompleted(@AuthenticationPrincipal LoginUser loginUser) {
		userService.updateTutorialCompleted(loginUser);
		return ApiResponse.success();
	}

	/**
	 * 탈퇴 API
	 * 1) 소셜 로그인 연결 해제
	 * 2) 회원 정보 삭제
	 */
	@DeleteMapping("/withdraw")
	public ApiResponse<?> withdraw(@AuthenticationPrincipal LoginUser loginUser) {
		String resultMessage = userService.withdraw(loginUser);
		return ApiResponse.success(resultMessage);
	}
}
