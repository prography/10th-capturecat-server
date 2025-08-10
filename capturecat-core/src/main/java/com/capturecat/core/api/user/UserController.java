package com.capturecat.core.api.user;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.api.user.dto.UserReqDto;
import com.capturecat.core.api.user.dto.UserReqDto.JoinReqDto;
import com.capturecat.core.api.user.dto.UserReqDto.WithdrawReqDto;
import com.capturecat.core.api.user.dto.UserRespDto;
import com.capturecat.core.api.user.dto.UserRespDto.InfoRespDto;
import com.capturecat.core.api.user.dto.UserRespDto.JoinRespDto;
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
	 * 2) 탈퇴 사유 저장 - 실패해도 1,2 롤백 X (별도 TX)
	 * 3) 회원 및 관련 데이터 삭제
	 */
	@DeleteMapping("/withdraw")
	public ApiResponse<?> withdraw(@AuthenticationPrincipal LoginUser loginUser,
		@RequestBody WithdrawReqDto req, BindingResult bindingResult) {
		String resultMessage = userService.withdraw(loginUser, req.getReason().trim());
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
}
