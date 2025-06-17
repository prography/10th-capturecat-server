package com.capturecat.core.api.image.dto;

import com.capturecat.core.api.image.dto.ImageRespDto.ImageDto;
import com.capturecat.core.api.image.dto.ImageRespDto.ImageListDto;
import com.capturecat.core.domain.image.Image;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ImageMapper {
    private final ModelMapper modelMapper;

    public ImageDto toDto(Image image) {
        return modelMapper.map(image, ImageDto.class);
    }

    public Image toEntity(ImageDto imageDto) {
        return modelMapper.map(imageDto, Image.class);
    }

    public ImageListDto toDto(List<Image> images) {
        List<ImageDto> dtoList = images.stream()
                .map(image -> modelMapper.map(image, ImageDto.class))
                .toList();
        return new ImageListDto(dtoList);
    }
}
