package com.capturecat.core.service.image;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.capturecat.client.upload.DeleteException;
import com.capturecat.client.upload.FileUploader;
import com.capturecat.client.upload.UploadException;
import com.capturecat.core.api.image.dto.UploadItemRequest;
import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.tag.ImageTag;
import com.capturecat.core.domain.tag.ImageTagFactory;
import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.tag.Tag;
import com.capturecat.core.domain.tag.TagRegister;
import com.capturecat.core.domain.tag.TagRepository;
import com.capturecat.core.domain.tag.TagValidator;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;
import com.capturecat.core.support.response.CursorResponse;
import com.capturecat.core.support.util.CursorUtil;
import com.capturecat.core.support.util.DateTimeConverter;

@Service
@RequiredArgsConstructor
public class ImageService {

	private final FileUploader fileUploader;
	private final ImageRepository imageRepository;
	private final ImageTagRepository imageTagRepository;
	private final ImageTagFactory imageTagFactory;
	private final TagValidator tagValidator;
	private final TagRegister tagRegister;
	private final TagRepository tagRepository;
	private final UserRepository userRepository;

	@Transactional
	// TODO: UploadItemRequest의 api 패키지 의존성 제거 고민하기 및 트랜잭션 분리
	public void save(List<UploadItemRequest> uploadItems, List<MultipartFile> files, LoginUser loginUser) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));

		List<Image> images = new ArrayList<>(files.size());
		for (MultipartFile file : files) {
			validate(file);
			String fileUrl = upload(file);

			UploadItemRequest uploadItemRequest = getMatchingUploadRequest(uploadItems, file.getOriginalFilename());

			Image image = Image.builder()
				.fileName(file.getOriginalFilename())
				.fileUrl(fileUrl)
				.size(file.getSize())
				.captureDate(DateTimeConverter.convert(uploadItemRequest.captureDate()))
				.user(user)
				.build();
			images.add(image);
		}

		List<Image> savedImages = imageRepository.saveAll(images);

		List<ImageTag> allImageTags = new ArrayList<>();
		for (Image savedImage : savedImages) {
			UploadItemRequest uploadItemRequest = getMatchingUploadRequest(uploadItems, savedImage.getFileName());
			List<String> tagNames = uploadItemRequest.tagNames();

			tagValidator.validateTagNames(savedImage, tagNames);

			List<Tag> result = tagRegister.registerTagsFor(tagNames);
			allImageTags.addAll(imageTagFactory.create(savedImage, result));
		}
		imageTagRepository.saveAll(allImageTags);
	}

	@Transactional
	public void addTagsToImage(Long imageId, List<String> tagNames, LoginUser loginUser) {
		validateUserExists(loginUser);

		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CoreException(ErrorType.IMAGE_NOT_FOUND));

		tagValidator.validateTagNames(image, tagNames);

		List<Tag> newTags = tagRegister.registerTagsFor(tagNames);
		List<ImageTag> imageTags = imageTagFactory.create(image, newTags);
		imageTagRepository.saveAll(imageTags);
	}

	@Transactional(readOnly = true)
	public CursorResponse<ImageWithTagsResponse> getImagesWithTags(LoginUser loginUser, Pageable pageable) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));

		Slice<ImageWithTagsResponse> responses = imageRepository.searchByUser(user, pageable)
			.map(ImageWithTagsResponse::of);

		return CursorUtil.toCursorResponse(responses, ImageWithTagsResponse::id);
	}

	@Transactional
	public void removeTagToImage(Long imageId, Long tagId, LoginUser loginUser) {
		validateUserExists(loginUser);

		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CoreException(ErrorType.IMAGE_NOT_FOUND));
		Tag tag = tagRepository.findById(tagId)
			.orElseThrow(() -> new CoreException(ErrorType.TAG_NOT_FOUND));
		ImageTag imageTag = imageTagRepository.findByImageAndTag(image, tag)
			.orElseThrow(() -> new CoreException(ErrorType.IMAGE_TAG_NOT_FOUND));

		imageTagRepository.delete(imageTag);
	}

	@Transactional(readOnly = true)
	public CursorResponse<ImageWithTagsResponse> searchImagesByTagNames(List<String> tagNames, LoginUser loginUser,
		Pageable pageable) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));

		Slice<ImageWithTagsResponse> responses = imageRepository.searchImagesByUserAndTagNames(user, tagNames, pageable)
			.map(ImageWithTagsResponse::of);

		return CursorUtil.toCursorResponse(responses, ImageWithTagsResponse::id);
	}

	@Transactional(readOnly = true)
	public ImageWithTagsResponse getImageWithTags(Long imageId, LoginUser loginUser) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CoreException(ErrorType.IMAGE_NOT_FOUND));

		image.validateOwnership(user);

		List<ImageTag> imageTags = imageTagRepository.findByImage(image);

		return new ImageWithTagsResponse(image.getId(), image.getFileName(), image.getFileUrl(), image.getCaptureDate(),
			imageTags.stream()
				.map(it -> TagResponse.from(it.getTag()))
				.toList());
	}

	@Transactional
	public void removeImages(Long imageId, LoginUser loginUser) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CoreException(ErrorType.IMAGE_NOT_FOUND));

		image.validateOwnership(user);

		delete(image.getFileName());
		imageRepository.delete(image);
		imageTagRepository.deleteAllByImage(image);
	}

	private void validate(MultipartFile file) {
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new CoreException(ErrorType.INVALID_IMAGE_FORMAT);
		}
	}

	private UploadItemRequest getMatchingUploadRequest(List<UploadItemRequest> uploadItems, String fileName) {
		return uploadItems.stream()
			.filter(i -> {
				System.out.println("i.fileName() = " + i.fileName());
				System.out.println("fileName = " + fileName);
				return i.fileName().equals(fileName);
			})
			.findFirst()
			.orElseThrow(() -> new CoreException(ErrorType.UPLOAD_METADATA_MISMATCH));
	}

	private String upload(MultipartFile file) {
		try {
			return fileUploader.upload(file);
		} catch (UploadException e) {
			throw new CoreException(ErrorType.IMAGE_UPLOAD_FAILED);
		}
	}

	private void validateUserExists(LoginUser loginUser) {
		if (userRepository.findByUsername(loginUser.getUsername()).isEmpty()) {
			throw new CoreException(ErrorType.USER_NOT_FOUND);
		}
	}

	private void delete(String name) {
		try {
			fileUploader.delete(name);
		} catch (DeleteException e) {
			throw new CoreException(ErrorType.IMAGE_DELETE_FAILED);
		}
	}
}
