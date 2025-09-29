package com.zotly.zotlychatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.zotly.zotlychatservice.entity.Conversation;
import com.zotly.zotlychatservice.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class ConversationSubscriberService {

    private final Mqtt5AsyncClient client;
    private final ConversationRepository conversationRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ConversationSubscriberService(Mqtt5AsyncClient client, ConversationRepository conversationRepository, ObjectMapper objectMapper) {
        this.client = client;
        this.conversationRepository = conversationRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void subscribe() {
        client.subscribeWith()
                .topicFilter("zotly/conversations/#")
                .callback(publish -> {
                    try {
                        String payload = new String(publish.getPayloadAsBytes());
                        System.out.println("📩 Received MQTT on conversations topic: " + payload);

                        // Only process if it's a full object (not delete/status strings); skip if payload is simple string
                        if (payload.startsWith("\"") && payload.endsWith("\"")) {
                            System.out.println("Skipping simple string payload (e.g., delete/status)");
                            return;
                        }

                        // Convert MQTT payload to Conversation and save
                        Conversation conversation = objectMapper.readValue(payload, Conversation.class);
                        conversationRepository.save(conversation);
                    } catch (Exception e) {
                        System.err.println("❌ Failed to process MQTT conversation: " + e.getMessage());
                    }
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        System.err.println("❌ Conversations subscription failed: " + throwable.getMessage());
                    } else {
                        System.out.println("✅ Subscribed to zotly/conversations/#");
                    }
                });
    }
}