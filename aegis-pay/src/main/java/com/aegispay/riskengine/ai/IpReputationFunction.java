package com.aegispay.riskengine.ai;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * External IP-reputation check, exposed to the LLM via Spring AI tool calling.
 * Wrapped in Resilience4j so a flaky vendor degrades the risk score's
 * confidence instead of failing the payment path.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IpReputationFunction {

    private final RestClient ipReputationRestClient;

    public record Request(String ipAddress) {}

    public record Response(String ipAddress, int reputationScore, String country, boolean isVpnOrProxy) {}

    @Tool(description = "Check the fraud reputation of an IP address. reputationScore: 0 = clean, 100 = known bad.")
    @Retry(name = "ip-reputation")
    @CircuitBreaker(name = "ip-reputation", fallbackMethod = "fallback")
    public Response checkIpReputation(Request request) {
        return ipReputationRestClient.get()
                .uri("/reputation/{ip}", request.ipAddress())
                .retrieve()
                .body(Response.class);
    }

    /** Open circuit / exhausted retries → neutral reputation, flagged as unavailable. */
    private Response fallback(Request request, Throwable t) {
        log.warn("IP reputation check unavailable for {} ({}), assuming neutral score",
                request.ipAddress(), t.getClass().getSimpleName());
        return new Response(request.ipAddress(), 50, "UNKNOWN", false);
    }
}
