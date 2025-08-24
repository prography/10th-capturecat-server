package com.capturecat.core.domain.image;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.api.image.dto.ImageRequestDto;
import com.capturecat.core.domain.annotation.DomainService;
import com.capturecat.core.domain.tag.ImageTag;
import com.capturecat.core.domain.tag.ImageTagFactory;
import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@DomainService
@RequiredArgsConstructor
public class ImageCreator {

	private final UserRepository userRepository;
	private final ImageRepository imageRepository;
	private final ImageTagRepository imageTagRepository;
	private final ImageTagValidator imageTagValidator;
	private final ImageTagFactory imageTagFactory;

	/**
	 * 이미지와 태그를 함께 저장합니다.
	 */
	@Transactional
	public List<Image> save(LoginUser loginUser, List<ImageRequestDto.ImageCreateData> requests) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));

		List<Image> savedImages = imageRepository.saveAll(requests.stream()
			.map(item -> Image.create(item.imageSaveRequest(), user))
			.toList());

		Map<String, Image> savedImagesMap = savedImages.stream()
			.collect(Collectors.toMap(Image::getFileName, Function.identity()));

		List<ImageTag> allImageTags = new ArrayList<>();
		for (ImageRequestDto.ImageCreateData request : requests) {
			Image image = savedImagesMap.get(request.imageSaveRequest().fileName());

			imageTagValidator.validateTags(image, request.tags());

			allImageTags.addAll(imageTagFactory.create(image, request.tags()));
		}
		imageTagRepository.saveAll(allImageTags);

		return savedImages;
	}
}
