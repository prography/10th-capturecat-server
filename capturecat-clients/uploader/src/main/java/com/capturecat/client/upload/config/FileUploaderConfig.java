package com.capturecat.client.upload.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class FileUploaderConfig {

	@Bean
	@Profile({ "dev", "prod" })
	public S3Client s3Client() {
		// AWS SDK는 AWS_ACCESS_KEY를 자동 인식
		// EC2 환경 변수 또는 ~/.aws/credentials, IAM Role 등 외부에 저장된 키를 사용함
		return S3Client.builder().region(Region.AP_NORTHEAST_2).build();
	}

}
