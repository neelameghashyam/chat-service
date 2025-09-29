package com.zotly.zotlychatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.zotly.zotlychatservice.dto.MessageDTO;
import com.zotly.zotlychatservice.service.impl.MessageServiceImpl;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class MessageSubscriberService {

    private final Mqtt5AsyncClient client;
    private final MessageServiceImpl messageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MessageSubscriberService(Mqtt5AsyncClient client, MessageServiceImpl messageService) {
        this.client = client;
        this.messageService = messageService;
    }

    @PostConstruct
    public void subscribe() {
        client.subscribeWith()
                .topicFilter("zotly/messages/#")
                .callback(publish -> {
                    try {
                        String payload = new String(publish.getPayloadAsBytes());
                        System.out.println("📩 Received MQTT on messages topic: " + payload);

                        // Convert MQTT payload to MessageDTO and save
                        MessageDTO dto = objectMapper.readValue(payload, MessageDTO.class);
                        messageService.create(dto);
                    } catch (Exception e) {
                        System.err.println("❌ Failed to process MQTT message: " + e.getMessage());
                    }
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        System.err.println("❌ Messages subscription failed: " + throwable.getMessage());
                    } else {
                        System.out.println("✅ Subscribed to zotly/messages/#");
                    }
                });
    }
}