package com.zotly.zotlychatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.zotly.zotlychatservice.entity.AgentTakeover;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MqttPublisherServiceTest {

    @Mock
    private Mqtt5AsyncClient client;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MqttPublisherService service;

    @Mock
    private Mqtt5PublishBuilder.Send<CompletableFuture<Mqtt5PublishResult>> sendBuilder;

    @Mock
    private Mqtt5PublishBuilder.Send.Complete<CompletableFuture<Mqtt5PublishResult>> sendComplete;

    @BeforeEach
    void setUp() {
        lenient().when(client.publishWith()).thenReturn(sendBuilder);
        lenient().when(sendBuilder.topic(anyString())).thenReturn(sendComplete);
        lenient().when(sendComplete.payload(any(byte[].class))).thenReturn(sendComplete);
        
        CompletableFuture<Mqtt5PublishResult> future = CompletableFuture.completedFuture(mock(Mqtt5PublishResult.class));
        lenient().when(sendComplete.send()).thenReturn(future);
    }

    @Test
    void publish_objectPayload_success() throws Exception {
        AgentTakeover payload = new AgentTakeover();
        payload.setTakeoverId(1L);
        String json = "{\"takeoverId\":1}";
        when(objectMapper.writeValueAsString(payload)).thenReturn(json);

        service.publish("topic/test", payload);

        Thread.sleep(100);

        verify(objectMapper).writeValueAsString(payload);
        verify(client).publishWith();
        verify(sendBuilder).topic("topic/test");
        verify(sendComplete).payload(json.getBytes(StandardCharsets.UTF_8));
        verify(sendComplete).send();
    }

    @Test
    void publish_stringPayload_success() {
        String payload = "test message";

        service.publish("topic/string", payload);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verifyNoInteractions(objectMapper);
        verify(client).publishWith();
        verify(sendBuilder).topic("topic/string");
        verify(sendComplete).payload(payload.getBytes(StandardCharsets.UTF_8));
        verify(sendComplete).send();
    }

    @Test
    void publish_jsonSerializationFailure_handlesException() throws Exception {
        AgentTakeover payload = new AgentTakeover();
        when(objectMapper.writeValueAsString(payload)).thenThrow(new RuntimeException("JSON error"));

        service.publish("topic/error", payload);

        verify(objectMapper).writeValueAsString(payload);
        verifyNoInteractions(client);
    }

    @Test
    void publish_publishFailure_handlesException() {
        reset(sendComplete);
        
        when(client.publishWith()).thenReturn(sendBuilder);
        when(sendBuilder.topic(anyString())).thenReturn(sendComplete);
        when(sendComplete.payload(any(byte[].class))).thenReturn(sendComplete);
        
        CompletableFuture<Mqtt5PublishResult> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("MQTT error"));
        when(sendComplete.send()).thenReturn(future);

        String payload = "test";
        service.publish("topic/fail", payload);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(sendComplete).send();
    }

    @Test
    void publish_emptyStringPayload_success() {
        String payload = "";

        service.publish("topic/empty", payload);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(client).publishWith();
        verify(sendComplete).send();
    }

    @Test
    void publish_complexObjectPayload_success() throws Exception {
        AgentTakeover payload = new AgentTakeover();
        payload.setTakeoverId(100L);
        payload.setConversationId(200L);
        payload.setFromAgent(1L);
        payload.setToAgent(2L);
        
        String json = "{\"takeoverId\":100,\"conversationId\":200,\"fromAgent\":1,\"toAgent\":2}";
        when(objectMapper.writeValueAsString(payload)).thenReturn(json);

        service.publish("zotly/agent-takeovers/100", payload);

        Thread.sleep(100);

        verify(objectMapper).writeValueAsString(payload);
        verify(sendComplete).send();
    }

    @Test
    void publish_nullPayload_handlesGracefully() throws Exception {
        when(objectMapper.writeValueAsString(null)).thenReturn("null");

        service.publish("topic/null", null);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(objectMapper).writeValueAsString(null);
        verify(sendComplete).send();
    }

    @Test
    void publish_specialCharactersInTopic_success() {
        String payload = "test";

        service.publish("zotly/messages/special-chars_123", payload);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(sendBuilder).topic("zotly/messages/special-chars_123");
        verify(sendComplete).send();
    }
}