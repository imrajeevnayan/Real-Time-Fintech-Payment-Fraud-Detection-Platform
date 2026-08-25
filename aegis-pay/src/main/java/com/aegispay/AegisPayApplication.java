package com.aegispay;

import com.aegispay.shared.kernel.Topics;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(Topics.class)
public class AegisPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(AegisPayApplication.class, args);
    }
}
