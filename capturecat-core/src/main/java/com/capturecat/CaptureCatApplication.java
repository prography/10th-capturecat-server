package com.capturecat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class CaptureCatApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaptureCatApplication.class, args);
    }

}
