package com.capturecat.core.domain.tag;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.annotation.DomainService;
import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageRepository;

@DomainService
@RequiredArgsConstructor
public class ImageTaggingDomainService {

	private final ImageRepository imageRepository;
	private final ImageTagRepository imageTagRepository;
	private final TagRepository tagRepository;
	private final ImageTagFactory imageTagFactory;

	@Transactional
	public void registerNewImagesWithTags(Image image, List<String> tagNames) {
		Image savedImage = imageRepository.save(image);
		List<Tag> existingTags = tagRepository.findByNameIn(tagNames);

		List<Tag> newTags = tagNames.stream()
			.filter(tagName -> isNewTag(existingTags, tagName))
			.map(Tag::new)
			.toList();

		List<Tag> savedNewTags = tagRepository.saveAll(newTags);

		List<Tag> allTags = new ArrayList<>(existingTags);
		allTags.addAll(savedNewTags);

		List<ImageTag> imageTags = imageTagFactory.create(savedImage, allTags);
		imageTagRepository.saveAll(imageTags);
	}

	@Transactional
	public void registerNewImagesWithTags(List<Image> images, List<String> tagNames) {
		List<Image> savedImages = imageRepository.saveAll(images);
		List<Tag> existingTags = tagRepository.findByNameIn(tagNames);

		List<Tag> newTags = tagNames.stream()
			.filter(tagName -> isNewTag(existingTags, tagName))
			.map(Tag::new)
			.toList();

		List<Tag> savedNewTags = tagRepository.saveAll(newTags);

		List<Tag> allTags = new ArrayList<>(existingTags);
		allTags.addAll(savedNewTags);

		List<ImageTag> allImageTags = new ArrayList<>(images.size() * allTags.size());
		for (Image savedImage : savedImages) {
			allImageTags.addAll(imageTagFactory.create(savedImage, allTags));
		}
		imageTagRepository.saveAll(allImageTags);
	}

	private boolean isNewTag(List<Tag> tags, String tagName) {
		return tags.stream()
			.noneMatch(t -> t.isSameNameAs(tagName));
	}
}
