package com.aegispay.riskengine.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dual-model wiring:
 *  - "fast"  → Ollama (local, sub-100ms triage pass)
 *  - "deep"  → OpenAI (accurate escalation pass with RAG context + function calling)
 * We build ChatClients from the auto-configured ChatModel beans directly to
 * avoid builder ambiguity when two model starters are on the classpath.
 */
@Configuration
public class RiskAiConfig {

    @Bean
    ChatClient deepChatClient(@Qualifier("openAiChatModel") org.springframework.ai.chat.model.ChatModel openAi,
                              IpReputationFunction ipReputationTool) {
        return ChatClient.builder(openAi)
                .defaultToolObjects(ipReputationTool)
                .defaultSystem("""
                        You are Aegis, a payment-fraud risk analyst.
                        You ALWAYS answer with strict JSON matching:
                        {"riskScore": <integer 0-100>, "reasons": ["..."], "anomalous": <boolean>}
                        Never include prose outside the JSON.
                        """)
                .build();
    }

    @Bean
    ChatClient fastChatClient(@Qualifier("ollamaChatModel") org.springframework.ai.chat.model.ChatModel ollama) {
        return ChatClient.builder(ollama)
                .defaultSystem("""
                        You are a fraud triage model. Consider only amount, channel, geography and velocity.
                        Answer ONLY with JSON: {"riskScore": <integer 0-100>, "reasons": ["..."], "anomalous": <boolean>}
                        """)
                .build();
    }
}
