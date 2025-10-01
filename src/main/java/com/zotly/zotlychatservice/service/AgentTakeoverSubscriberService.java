package com.zotly.zotlychatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.zotly.zotlychatservice.entity.AgentTakeover;
import com.zotly.zotlychatservice.repository.AgentTakeoverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@Profile("!test") // Exclude from test profile
public class AgentTakeoverSubscriberService {

    private final Mqtt5AsyncClient client;
    private final AgentTakeoverRepository agentTakeoverRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public AgentTakeoverSubscriberService(Mqtt5AsyncClient client, AgentTakeoverRepository agentTakeoverRepository, ObjectMapper objectMapper) {
        this.client = client;
        this.agentTakeoverRepository = agentTakeoverRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void subscribe() {
        client.subscribeWith()
                .topicFilter("zotly/agent-takeovers/#")
                .callback(publish -> {
                    try {
                        String payload = new String(publish.getPayloadAsBytes());
                        System.out.println("📩 Received MQTT on agent-takeovers topic: " + payload);

                        if (payload.startsWith("\"") && payload.endsWith("\"")) {
                            System.out.println("Skipping simple string payload (e.g., delete)");
                            return;
                        }

                        AgentTakeover takeover = objectMapper.readValue(payload, AgentTakeover.class);
                        agentTakeoverRepository.save(takeover);
                    } catch (Exception e) {
                        System.err.println("❌ Failed to process MQTT agent-takeover: " + e.getMessage());
                    }
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        System.err.println("❌ AgentTakeovers subscription failed: " + throwable.getMessage());
                    } else {
                        System.out.println("✅ Subscribed to zotly/agent-takeovers/#");
                    }
                });
    }
}