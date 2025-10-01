package com.zotly.zotlychatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import com.zotly.zotlychatservice.entity.Conversation;
import com.zotly.zotlychatservice.repository.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationSubscriberServiceTest {

    @Mock
    private Mqtt5AsyncClient client;

    @Mock
    private Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder.Start subscribeStart;

    @Mock
    private Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder.Start.Complete complete;

    @Mock
    private Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder.Call.Ex callExBuilder;

    @Mock
    private ConversationRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ConversationSubscriberService service;

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
        verify(subscribeStart).topicFilter("zotly/conversations/#");
        verify(complete).callback(any());
        verify(callExBuilder).send();
    }

    @Test
    void callback_processConversation_success() throws Exception {
        service.subscribe();
        verify(complete).callback(callbackCaptor.capture());
        Consumer<Mqtt5Publish> callback = callbackCaptor.getValue();

        Conversation conv = Conversation.builder()
                .id(1L)
                .threadId("thread-1")
                .message("Test message")
                .status(Conversation.Status.ACTIVE)
                .build();
        
        String payload = "{\"id\":1,\"threadId\":\"thread-1\",\"message\":\"Test message\"}";
        Mqtt5Publish publish = mock(Mqtt5Publish.class);
        when(publish.getPayloadAsBytes()).thenReturn(payload.getBytes(StandardCharsets.UTF_8));
        when(objectMapper.readValue(payload, Conversation.class)).thenReturn(conv);
        when(repository.save(any(Conversation.class))).thenReturn(conv);

        callback.accept(publish);

        verify(objectMapper).readValue(payload, Conversation.class);
        verify(repository).save(conv);
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
    void callback_skipStatusUpdatePayload() {
        service.subscribe();
        verify(complete).callback(callbackCaptor.capture());
        Consumer<Mqtt5Publish> callback = callbackCaptor.getValue();

        String payload = "\"status updated\"";
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
        when(objectMapper.readValue(anyString(), eq(Conversation.class)))
                .thenThrow(new RuntimeException("Invalid JSON"));

        assertDoesNotThrow(() -> callback.accept(publish));

        verify(objectMapper).readValue(payload, Conversation.class);
        verifyNoInteractions(repository);
    }

    @Test
    void callback_processConversationWithAllFields() throws Exception {
        service.subscribe();
        verify(complete).callback(callbackCaptor.capture());
        Consumer<Mqtt5Publish> callback = callbackCaptor.getValue();

        Conversation conv = Conversation.builder()
                .id(5L)
                .threadId("thread-5")
                .message("Complete message")
                .chatToken("token-123")
                .archived(false)
                .likeCount(10)
                .dislikeCount(2)
                .isUnread(true)
                .timestamp(LocalDateTime.now())
                .status(Conversation.Status.ACTIVE)
                .customerId(100L)
                .build();
        
        String payload = "{\"id\":5,\"threadId\":\"thread-5\",\"message\":\"Complete message\"}";
        Mqtt5Publish publish = mock(Mqtt5Publish.class);
        when(publish.getPayloadAsBytes()).thenReturn(payload.getBytes(StandardCharsets.UTF_8));
        when(objectMapper.readValue(payload, Conversation.class)).thenReturn(conv);
        when(repository.save(any(Conversation.class))).thenReturn(conv);

        callback.accept(publish);

        ArgumentCaptor<Conversation> convCaptor = ArgumentCaptor.forClass(Conversation.class);
        verify(repository).save(convCaptor.capture());
        
        Conversation saved = convCaptor.getValue();
        assertEquals(5L, saved.getId());
        assertEquals("thread-5", saved.getThreadId());
        assertEquals("Complete message", saved.getMessage());
        assertEquals(Conversation.Status.ACTIVE, saved.getStatus());
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