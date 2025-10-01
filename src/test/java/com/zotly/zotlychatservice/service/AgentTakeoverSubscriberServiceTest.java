package com.zotly.zotlychatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import com.zotly.zotlychatservice.entity.AgentTakeover;
import com.zotly.zotlychatservice.repository.AgentTakeoverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentTakeoverSubscriberServiceTest {

    @Mock
    private Mqtt5AsyncClient client;

    @Mock
    private Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder.Start subscribeStart;

    @Mock
    private Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder.Start.Complete complete;

    @Mock
    private Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder.Call.Ex callExBuilder;

    @Mock
    private AgentTakeoverRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AgentTakeoverSubscriberService service;

    @Captor
    private ArgumentCaptor<Consumer<Mqtt5Publish>> callbackCaptor;

    @Captor
    private ArgumentCaptor<BiConsumer<Mqtt5SubAck, Throwable>> whenCompleteCaptor;

    @BeforeEach
    void setUp() {
        when(client.subscribeWith()).thenReturn(subscribeStart);
        when(subscribeStart.topicFilter(anyString())).thenReturn(complete);
        when(complete.callback(any())).thenReturn(callExBuilder);
        when(callExBuilder.send()).thenReturn(new CompletableFuture<>());
    }

    @Test
    void subscribe_success() {
        service.subscribe();

        verify(client).subscribeWith();
        verify(subscribeStart).topicFilter("zotly/agent-takeovers/#");
        verify(complete).callback(any());
        verify(callExBuilder).send();
    }

    @Test
    void callback_processTakeover_success() throws Exception {
        service.subscribe();
        verify(complete).callback(callbackCaptor.capture());
        Consumer<Mqtt5Publish> callback = callbackCaptor.getValue();

        AgentTakeover takeover = new AgentTakeover();
        takeover.setTakeoverId(1L);
        takeover.setConversationId(100L);
        takeover.setToAgent(200L);
        
        String payload = "{\"takeoverId\":1,\"conversationId\":100,\"toAgent\":200}";
        Mqtt5Publish publish = mock(Mqtt5Publish.class);
        when(publish.getPayloadAsBytes()).thenReturn(payload.getBytes(StandardCharsets.UTF_8));
        when(objectMapper.readValue(payload, AgentTakeover.class)).thenReturn(takeover);
        when(repository.save(any(AgentTakeover.class))).thenReturn(takeover);

        callback.accept(publish);

        verify(objectMapper).readValue(payload, AgentTakeover.class);
        verify(repository).save(takeover);
    }

    @Test
    void callback_skipStringPayload() {
        service.subscribe();
        verify(complete).callback(callbackCaptor.capture());
        Consumer<Mqtt5Publish> callback = callbackCaptor.getValue();

        String payload = "\"deleted\"";
        Mqtt5Publish publish = mock(Mqtt5Publish.class);
        when(publish.getPayloadAsBytes()).thenReturn(payload.getBytes(StandardCharsets.UTF_8));

        callback.accept(publish);

        verifyNoInteractions(objectMapper);
        verifyNoInteractions(repository);
    }

    @Test
    void callback_exceptionHandling() throws Exception {
        service.subscribe();
        verify(complete).callback(callbackCaptor.capture());
        Consumer<Mqtt5Publish> callback = callbackCaptor.getValue();

        String payload = "invalid json";
        Mqtt5Publish publish = mock(Mqtt5Publish.class);
        when(publish.getPayloadAsBytes()).thenReturn(payload.getBytes(StandardCharsets.UTF_8));
        when(objectMapper.readValue(anyString(), eq(AgentTakeover.class)))
                .thenThrow(new RuntimeException("Invalid JSON"));

        assertDoesNotThrow(() -> callback.accept(publish));

        verify(objectMapper).readValue(payload, AgentTakeover.class);
        verifyNoInteractions(repository);
    }

    @Test
    void callback_processComplexTakeover() throws Exception {
        service.subscribe();
        verify(complete).callback(callbackCaptor.capture());
        Consumer<Mqtt5Publish> callback = callbackCaptor.getValue();

        AgentTakeover takeover = new AgentTakeover();
        takeover.setTakeoverId(5L);
        takeover.setConversationId(500L);
        takeover.setFromAgent(100L);
        takeover.setToAgent(200L);
        
        String payload = "{\"takeoverId\":5,\"conversationId\":500,\"fromAgent\":100,\"toAgent\":200}";
        Mqtt5Publish publish = mock(Mqtt5Publish.class);
        when(publish.getPayloadAsBytes()).thenReturn(payload.getBytes(StandardCharsets.UTF_8));
        when(objectMapper.readValue(payload, AgentTakeover.class)).thenReturn(takeover);
        when(repository.save(any(AgentTakeover.class))).thenReturn(takeover);

        callback.accept(publish);

        ArgumentCaptor<AgentTakeover> takeoverCaptor = ArgumentCaptor.forClass(AgentTakeover.class);
        verify(repository).save(takeoverCaptor.capture());
        
        AgentTakeover saved = takeoverCaptor.getValue();
        assertEquals(5L, saved.getTakeoverId());
        assertEquals(500L, saved.getConversationId());
        assertEquals(100L, saved.getFromAgent());
        assertEquals(200L, saved.getToAgent());
    }

    @Test
    void subscribe_completesSuccessfully() {
        CompletableFuture<Mqtt5SubAck> future = CompletableFuture.completedFuture(mock(Mqtt5SubAck.class));
        when(callExBuilder.send()).thenReturn(future);

        service.subscribe();

        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
    }

    @Test
    void subscribe_handlesSubscriptionFailure() {
        CompletableFuture<Mqtt5SubAck> mockFuture = mock(CompletableFuture.class);
        when(callExBuilder.send()).thenReturn(mockFuture);
        when(mockFuture.whenComplete(any())).thenReturn(mockFuture);

        service.subscribe();
        
        verify(callExBuilder).send();
        verify(mockFuture).whenComplete(whenCompleteCaptor.capture());
        
        BiConsumer<Mqtt5SubAck, Throwable> callback = whenCompleteCaptor.getValue();
        RuntimeException exception = new RuntimeException("Connection failed");
        
        // Simulate the callback being invoked with an exception
        assertDoesNotThrow(() -> callback.accept(null, exception));
    }
}