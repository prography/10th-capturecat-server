package com.capturecat.core.config;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class AppConfig {

    @Bean
    @Profile({"dev", "prod"})
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .build(); // ~/.aws/credentials의 default 프로파일 사용
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        // 전역 Converter: LocalDateTime -> String
        Converter<LocalDateTime, String> dateTimeToString = ctx ->
                ctx.getSource() == null ? null : ctx.getSource().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 모든 LocalDateTime → String 매핑에 TypeMap 강제 등록
        modelMapper.createTypeMap(LocalDateTime.class, String.class).setConverter(dateTimeToString);

        return modelMapper;
    }
}
