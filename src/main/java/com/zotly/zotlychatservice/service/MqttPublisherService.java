package com.zotly.zotlychatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class MqttPublisherService {

    private final Mqtt5AsyncClient client;
    private final ObjectMapper objectMapper;

    @Autowired
    public MqttPublisherService(Mqtt5AsyncClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    public void publish(String topic, Object payload) {
        try {
            String message;
            if (payload instanceof String) {
                message = (String) payload;
            } else {
                message = objectMapper.writeValueAsString(payload);
            }
            client.publishWith()
                    .topic(topic)
                    .payload(message.getBytes(StandardCharsets.UTF_8))
                    .send()
                    .whenComplete((ack, throwable) -> {
                        if (throwable != null) {
                            System.err.println("❌ MQTT publish failed: " + throwable.getMessage());
                        } else {
                            System.out.println("✅ Published to topic: " + topic);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}