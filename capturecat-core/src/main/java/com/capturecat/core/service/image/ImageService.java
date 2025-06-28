package com.capturecat.core.service.image;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.capturecat.client.upload.FileUploader;
import com.capturecat.core.api.image.dto.UploadItemRequest;
import com.capturecat.core.config.auth.LoginUser;
import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.tag.ImageTag;
import com.capturecat.core.domain.tag.ImageTagFactory;
import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.tag.Tag;
import com.capturecat.core.domain.tag.TagRegister;
import com.capturecat.core.domain.tag.TagValidator;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;
import com.capturecat.core.support.response.CursorResponse;

@Service
@RequiredArgsConstructor
public class ImageService {

	private final FileUploader fileUploader;
	private final ImageRepository imageRepository;
	private final ImageTagRepository imageTagRepository;
	private final ImageTagFactory imageTagFactory;
	private final TagValidator tagValidator;
	private final TagRegister tagRegister;
	private final UserRepository userRepository;

	@Transactional
	public void save(List<UploadItemRequest> uploadItems, List<MultipartFile> files) {
		List<Image> images = new ArrayList<>(files.size());
		for (MultipartFile file : files) {
			validate(file);
			String fileUrl = fileUploader.upload(file);

			Image image = Image.builder()
				.fileName(file.getOriginalFilename())
				.fileUrl(fileUrl)
				.size(file.getSize())
				.build();
			images.add(image);
		}

		List<Image> savedImages = imageRepository.saveAll(images);

		List<ImageTag> allImageTags = new ArrayList<>();
		for (Image savedImage : savedImages) {
			List<String> tagNames = uploadItems.stream()
				.filter(i -> savedImage.isSameFileNameAs(i.fileName()))
				.map(UploadItemRequest::tagNames)
				.findFirst()
				.orElseThrow(() -> new CoreException(ErrorType.TAG_INFO_MISMATCH));

			tagValidator.validateTagNames(savedImage, tagNames);

			List<Tag> result = tagRegister.registerTagsFor(tagNames);
			allImageTags.addAll(imageTagFactory.create(savedImage, result));
		}
		imageTagRepository.saveAll(allImageTags);
	}

	@Transactional
	public void addTagsToImage(Long imageId, List<String> tagNames) {
		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CoreException(ErrorType.IMAGE_NOT_FOUND));

		tagValidator.validateTagNames(image, tagNames);

		List<Tag> newTags = tagRegister.registerTagsFor(tagNames);
		List<ImageTag> imageTags = imageTagFactory.create(image, newTags);
		imageTagRepository.saveAll(imageTags);
	}

	@Transactional(readOnly = true)
	public CursorResponse<ImageWithTagsResponse> getImagesWithTags(Pageable pageable) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		LoginUser loginUser = (LoginUser)authentication.getPrincipal();
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));

		Slice<ImageWithTagsResponse> responses = imageRepository.searchByUser(user, pageable);
		if (responses.isEmpty()) {
			return CursorResponse.empty();
		} else {
			Long lastCursor = responses.getContent().getLast().id();
			return CursorResponse.of(responses, lastCursor);
		}
	}

	@Transactional
	public void removeTagsToImage(Long imageId, List<Long> tagIds) {
		Image image = imageRepository.findById(imageId).orElseThrow(() -> new CoreException(ErrorType.IMAGE_NOT_FOUND));
		List<ImageTag> imageTags = imageTagRepository.findByImageAndTagIds(image, tagIds);
		if (imageTags.isEmpty()) {
			return;
		}
		imageTagRepository.deleteAll(imageTags);
	}

	private void validate(MultipartFile file) {
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new CoreException(ErrorType.INVALID_IMAGE_FORMAT);
		}
	}
}
