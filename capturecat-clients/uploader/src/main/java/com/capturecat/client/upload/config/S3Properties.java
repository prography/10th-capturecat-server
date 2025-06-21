package com.capturecat.client.upload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;

@Profile({ "dev", "prod" })
@Validated
@ConfigurationProperties(prefix = "image.s3")
public record S3Properties(@NotEmpty String bucket, @NotEmpty String dirPrefix, @NotEmpty String urlPrefix) {
}
