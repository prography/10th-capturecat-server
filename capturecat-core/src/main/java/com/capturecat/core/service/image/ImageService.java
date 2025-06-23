package com.capturecat.core.service.image;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.capturecat.client.upload.FileUploader;
import com.capturecat.core.api.image.dto.ImageAndTagUploadRequest;
import com.capturecat.core.api.image.dto.ImageMapper;
import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.tag.ImageTag;
import com.capturecat.core.domain.tag.ImageTagFactory;
import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.tag.ImageTaggingDomainService;
import com.capturecat.core.domain.tag.Tag;
import com.capturecat.core.domain.tag.TagMaxCountValidator;
import com.capturecat.core.domain.tag.TagRepository;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Service
@RequiredArgsConstructor
public class ImageService {

	private final FileUploader fileUploader;
	private final ImageRepository imageRepository;
	private final ImageTagRepository imageTagRepository;
	private final TagRepository tagRepository;
	private final ImageTagFactory imageTagFactory;
	private final TagMaxCountValidator tagMaxCountValidator;
	private final ImageTaggingDomainService imageTaggingDomainService;
	private final ImageMapper mapper;

	public void save(List<ImageAndTagUploadRequest.Request> requests) {
		for (ImageAndTagUploadRequest.Request request : requests) {
			validate(request.getFile());

			String fileUrl = fileUploader.upload(request.getFile());

			Image image = Image.builder()
				.fileName(request.getFile().getOriginalFilename())
				.fileUrl(fileUrl)
				.size(request.getFile().getSize())
				.build();
			imageTaggingDomainService.registerNewImagesWithTags(image, request.getTagNames());
		}
	}

	@Transactional
	public void addTagsToImage(Long imageId, List<String> tagNames) {
		Image image = imageRepository.findById(imageId).orElseThrow(() -> new CoreException(ErrorType.IMAGE_NOT_FOUND));

		Set<String> existingTagNames = new HashSet<>(imageTagRepository.findTagNamesByImage(image));

		tagMaxCountValidator.validate(existingTagNames, tagNames);

		List<Tag> newTags = tagNames.stream()
			.filter(tagName -> !existingTagNames.contains(tagName))
			.map(Tag::new)
			.toList();

		tagRepository.saveAll(newTags);
		List<ImageTag> imageTags = imageTagFactory.create(image, newTags);
		imageTagRepository.saveAll(imageTags);
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
