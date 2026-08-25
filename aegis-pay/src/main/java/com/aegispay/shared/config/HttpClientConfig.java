package com.aegispay.shared.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {

    @Bean
    RestClient ipReputationRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("https://ip-reputation.internal.aegispay.io")
                .build();
    }
}
