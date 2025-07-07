package com.capturecat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan(basePackages = "com.capturecat.core.support")
@SpringBootApplication
public class CaptureCatApplication {

	public static void main(String[] args) {
		SpringApplication.run(CaptureCatApplication.class, args);
	}

}
