package com.capturecat.core;

import com.capturecat.core.domain.image.Image;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

public class DummyObject {

    public static Image newMockImage(int id) {
        return Image.builder()
                .id((long) id)
                .fileName("test1.jpg")
                .fileUrl("testUrl1")
                .build();
    }

    public static List<Image> newMockImages(int fromId, int toId) {
        return IntStream.range(fromId, toId + 1)
                .mapToObj(i -> {
                    Image image = Image.builder()
                            .id((long) i)
                            .fileName("test" + i)
                            .fileUrl("testUrl" + i)
                            .build();
                    ReflectionTestUtils.setField(image, "createdDate", LocalDateTime.now());
                    ReflectionTestUtils.setField(image, "lastModifiedDate", LocalDateTime.now());
                    return image;
                }).toList();
    }
}
