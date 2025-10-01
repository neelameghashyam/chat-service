package com.zotly.zotlychatservice.config;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestMqttConfig {

    @Bean
    @Primary
    public Mqtt5AsyncClient mockMqttClient() {
        // Return a simple mock - the actual MQTT operations won't be used in tests
        // because the subscriber services are excluded via @Profile("!test")
        return Mockito.mock(Mqtt5AsyncClient.class);
    }
}