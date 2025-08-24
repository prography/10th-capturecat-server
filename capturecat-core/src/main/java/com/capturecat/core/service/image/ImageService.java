package com.capturecat.core.service.image;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.capturecat.client.upload.DeleteException;
import com.capturecat.client.upload.FileUploader;
import com.capturecat.client.upload.UploadException;
import com.capturecat.core.api.image.dto.ImageRequestDto;
import com.capturecat.core.api.image.dto.UploadItemRequest;
import com.capturecat.core.domain.bookmark.Bookmark;
import com.capturecat.core.domain.bookmark.BookmarkRepository;
import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageCreator;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.image.dto.ImageSaveRequest;
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
	private final BookmarkRepository bookmarkRepository;
	private final ImageCreator imageCreator;

	@Transactional
	// TODO: UploadItemRequest의 api 패키지 의존성 제거 고민하기 및 트랜잭션 분리
	// 역할이 너무 많음.. 이미지 업로드 -> 이미지 저장 -> 즐겨찾기 -> 태그 저장 -> 이미지 태그 저장
	public void save(List<UploadItemRequest> uploadItems, List<MultipartFile> files, LoginUser loginUser) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));

		List<Image> images = new ArrayList<>(files.size());
		List<Bookmark> bookmarks = new ArrayList<>(files.size());
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

			if (uploadItemRequest.isBookmarked()) {
				bookmarks.add(new Bookmark(user, image));
			}
		}

		List<Image> savedImages = imageRepository.saveAll(images);
		bookmarkRepository.saveAll(bookmarks);

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

	public void save(List<ImageRequestDto.UploadItem> uploadItems, LoginUser loginUser) {
		// 1. 태그 등록
		List<String> allTagNames = uploadItems.stream()
			.flatMap(item -> item.tagNames().stream())
			.distinct()
			.toList();

		Map<String, Tag> registeredTags = tagRegister.registerTagsFor(allTagNames).stream()
			.collect(Collectors.toMap(Tag::getName, Function.identity()));

		// 2. 이미지 정보와 태그 엔티티 매핑
		List<ImageRequestDto.ImageCreateData> requests = uploadItems.stream()
			.map(each -> {
				// TODO: Pre-signed URL 및 fileURL 생성 필요
				ImageSaveRequest imageSaveRequest = ImageSaveRequest.builder()
					.fileName(each.fileName())
					.fileUrl("")
					.size(each.fileSize())
					.captureDate(DateTimeConverter.convert(each.captureDate()))
					.tagNames(each.tagNames())
					.build();

				List<Tag> tags = each.tagNames().stream()
					.map(registeredTags::get)
					.toList();

				return new ImageRequestDto.ImageCreateData(imageSaveRequest, tags);
			}).toList();

		// 3. 이미지 및 이미지 태그 저장
		imageCreator.save(loginUser, requests);

		// TODO: 응답 리턴(생성된 이미지 + pre-signed URL)
	}

	@Transactional
	public List<TagResponse> addTagsToImage(Long imageId, List<String> tagNames, LoginUser loginUser) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CoreException(ErrorType.IMAGE_NOT_FOUND));

		image.validateOwnership(user);
		tagValidator.validateTagNames(image, tagNames);

		List<Tag> newTags = tagRegister.registerTagsFor(tagNames);
		List<ImageTag> imageTags = imageTagFactory.create(image, newTags);
		imageTagRepository.saveAll(imageTags);

		return TagResponse.from(newTags);
	}

	@Transactional(readOnly = true)
	public CursorResponse<ImageWithTagsResponse> getImagesWithTags(LoginUser loginUser, Boolean hasTags,
		Pageable pageable) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));

		Slice<ImageWithTagsResponse> responses = imageRepository.searchByUser(user, hasTags, pageable)
			.map(ImageWithTagsResponse::from);

		return CursorUtil.toCursorResponse(responses, ImageWithTagsResponse::id);
	}

	@Transactional
	public void removeTagToImage(Long imageId, Long tagId, LoginUser loginUser) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CoreException(ErrorType.IMAGE_NOT_FOUND));

		image.validateOwnership(user);

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
			.map(ImageWithTagsResponse::from);

		return CursorUtil.toCursorResponse(responses, ImageWithTagsResponse::id);
	}

	@Transactional(readOnly = true)
	public ImageWithTagsResponse getImageWithTags(Long imageId, LoginUser loginUser) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CoreException(ErrorType.IMAGE_NOT_FOUND));
		boolean isBookmarked = bookmarkRepository.existsByUserAndImage(user, image);

		image.validateOwnership(user);

		List<ImageTag> imageTags = imageTagRepository.findByImage(image);

		return new ImageWithTagsResponse(image.getId(), image.getFileName(), image.getFileUrl(), image.getCaptureDate(),
			isBookmarked, convertToTagResponses(imageTags));
	}

	@Transactional
	public void removeImages(Long imageId, LoginUser loginUser) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CoreException(ErrorType.IMAGE_NOT_FOUND));

		image.validateOwnership(user);

		delete(image.getFileName());
		bookmarkRepository.deleteByUserAndImage(user, image);
		imageTagRepository.deleteAllByImage(image);
		imageRepository.delete(image);
	}

	private void validate(MultipartFile file) {
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new CoreException(ErrorType.INVALID_IMAGE_FORMAT);
		}
	}

	private UploadItemRequest getMatchingUploadRequest(List<UploadItemRequest> uploadItems, String fileName) {
		return uploadItems.stream()
			.filter(i -> i.fileName().equals(fileName))
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

	private void delete(String name) {
		try {
			fileUploader.delete(name);
		} catch (DeleteException e) {
			throw new CoreException(ErrorType.IMAGE_DELETE_FAILED);
		}
	}

	private List<TagResponse> convertToTagResponses(List<ImageTag> imageTags) {
		return imageTags.stream()
			.map(it -> TagResponse.from(it.getTag()))
			.toList();
	}
}
