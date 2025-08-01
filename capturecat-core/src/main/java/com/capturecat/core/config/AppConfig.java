package com.capturecat.core.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();

		modelMapper.getConfiguration()
			.setFieldMatchingEnabled(true)
			.setFieldAccessLevel(AccessLevel.PRIVATE)
			.setMatchingStrategy(MatchingStrategies.STRICT);

		// 전역 Converter: LocalDateTime -> String
		Converter<LocalDateTime, String> dateTimeToString = ctx -> ctx.getSource() == null ? null
			: ctx.getSource().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

		// 모든 LocalDateTime → String 매핑에 TypeMap 강제 등록
		modelMapper.createTypeMap(LocalDateTime.class, String.class).setConverter(dateTimeToString);

		return modelMapper;
	}

	@Bean
	public WebClient webClient() {
		return WebClient.builder().build();
	}
}
