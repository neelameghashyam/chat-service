package com.zotly.zotlychatservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import com.zotly.zotlychatservice.dto.MessageDTO;
import com.zotly.zotlychatservice.service.impl.MessageServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageSubscriberServiceTest {

    @Mock
    private Mqtt5AsyncClient client;

    @Mock
    private Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder.Start subscribeStart;

    @Mock
    private Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder.Start.Complete complete;

    @Mock
    private Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder.Call.Ex callExBuilder;

    @Mock
    private CompletableFuture<Mqtt5SubAck> subscriptionFuture;

    @Mock
    private MessageServiceImpl messageService;

    @InjectMocks
    private MessageSubscriberService service;

    @Captor
    private ArgumentCaptor<Consumer<Mqtt5Publish>> callbackCaptor;

    @Captor
    private ArgumentCaptor<BiConsumer<Mqtt5SubAck, Throwable>> whenCompleteCaptor;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        when(client.subscribeWith()).thenReturn(subscribeStart);
        when(subscribeStart.topicFilter(anyString())).thenReturn(complete);
        when(complete.callback(any())).thenReturn(callExBuilder);
        when(callExBuilder.send()).thenReturn(subscriptionFuture);
    }

    @Test
    void subscribe_success() {
        service.subscribe();

        verify(client).subscribeWith();
        verify(subscribeStart).topicFilter("zotly/messages/#");
        verify(complete).callback(any());
        verify(callExBuilder).send();
    }

    @Test
    void callback_processMessage_success() throws Exception {
        service.subscribe();
        verify(complete).callback(callbackCaptor.capture());
        Consumer<Mqtt5Publish> callback = callbackCaptor.getValue();

        MessageDTO dto = new MessageDTO(1L, 1L, "USER", 1L, "Hello", "TEXT");
        String payload = "{\"messageId\":1,\"conversationId\":1,\"senderType\":\"USER\",\"senderId\":1,\"content\":\"Hello\",\"messageType\":\"TEXT\"}";
        Mqtt5Publish publish = mock(Mqtt5Publish.class);
        when(publish.getPayloadAsBytes()).thenReturn(payload.getBytes(StandardCharsets.UTF_8));
        when(messageService.create(any(MessageDTO.class))).thenReturn(dto);

        callback.accept(publish);

        verify(messageService).create(any(MessageDTO.class));
    }

    @Test
    void callback_exceptionHandling() throws Exception {
        service.subscribe();
        verify(complete).callback(callbackCaptor.capture());
        Consumer<Mqtt5Publish> callback = callbackCaptor.getValue();

        String payload = "invalid json";
        Mqtt5Publish publish = mock(Mqtt5Publish.class);
        when(publish.getPayloadAsBytes()).thenReturn(payload.getBytes(StandardCharsets.UTF_8));

        assertDoesNotThrow(() -> callback.accept(publish));

        verifyNoInteractions(messageService);
    }

    @Test
    void callback_processMessageWithAllFields() throws Exception {
        service.subscribe();
        verify(complete).callback(callbackCaptor.capture());
        Consumer<Mqtt5Publish> callback = callbackCaptor.getValue();

        MessageDTO dto = new MessageDTO(5L, 100L, "AGENT", 200L, "Complete message", "TEXT");
        String payload = "{\"messageId\":5,\"conversationId\":100,\"senderType\":\"AGENT\",\"senderId\":200,\"content\":\"Complete message\",\"messageType\":\"TEXT\"}";
        Mqtt5Publish publish = mock(Mqtt5Publish.class);
        when(publish.getPayloadAsBytes()).thenReturn(payload.getBytes(StandardCharsets.UTF_8));
        when(messageService.create(any(MessageDTO.class))).thenReturn(dto);

        callback.accept(publish);

        ArgumentCaptor<MessageDTO> dtoCaptor = ArgumentCaptor.forClass(MessageDTO.class);
        verify(messageService).create(dtoCaptor.capture());
        
        MessageDTO captured = dtoCaptor.getValue();
        assertNotNull(captured);
        assertEquals(100L, captured.conversationId());
        assertEquals("AGENT", captured.senderType());
        assertEquals(200L, captured.senderId());
        assertEquals("Complete message", captured.content());
        assertEquals("TEXT", captured.messageType());
    }

    @Test
    void callback_processMessageFromCustomer() throws Exception {
        service.subscribe();
        verify(complete).callback(callbackCaptor.capture());
        Consumer<Mqtt5Publish> callback = callbackCaptor.getValue();

        MessageDTO dto = new MessageDTO(null, 50L, "CUSTOMER", 300L, "Customer inquiry", "TEXT");
        String payload = "{\"conversationId\":50,\"senderType\":\"CUSTOMER\",\"senderId\":300,\"content\":\"Customer inquiry\",\"messageType\":\"TEXT\"}";
        Mqtt5Publish publish = mock(Mqtt5Publish.class);
        when(publish.getPayloadAsBytes()).thenReturn(payload.getBytes(StandardCharsets.UTF_8));
        when(messageService.create(any(MessageDTO.class))).thenReturn(dto);

        callback.accept(publish);

        ArgumentCaptor<MessageDTO> dtoCaptor = ArgumentCaptor.forClass(MessageDTO.class);
        verify(messageService).create(dtoCaptor.capture());
        
        MessageDTO captured = dtoCaptor.getValue();
        assertEquals("CUSTOMER", captured.senderType());
        assertEquals("Customer inquiry", captured.content());
    }

    @Test
    void subscribe_completesSuccessfully() {
        when(subscriptionFuture.whenComplete(whenCompleteCaptor.capture())).thenReturn(subscriptionFuture);
        
        service.subscribe();

        BiConsumer<Mqtt5SubAck, Throwable> whenComplete = whenCompleteCaptor.getValue();
        whenComplete.accept(mock(Mqtt5SubAck.class), null);

        verify(subscriptionFuture).whenComplete(any());
    }

    @Test
    void subscribe_handlesSubscriptionFailure() {
        when(subscriptionFuture.whenComplete(whenCompleteCaptor.capture())).thenReturn(subscriptionFuture);

        service.subscribe();

        BiConsumer<Mqtt5SubAck, Throwable> whenComplete = whenCompleteCaptor.getValue();
        RuntimeException exception = new RuntimeException("Connection failed");
        
        assertDoesNotThrow(() -> whenComplete.accept(null, exception));

        verify(subscriptionFuture).whenComplete(any());
    }

    @Test
    void callback_emptyPayload_handlesGracefully() {
        service.subscribe();
        verify(complete).callback(callbackCaptor.capture());
        Consumer<Mqtt5Publish> callback = callbackCaptor.getValue();

        String payload = "";
        Mqtt5Publish publish = mock(Mqtt5Publish.class);
        when(publish.getPayloadAsBytes()).thenReturn(payload.getBytes(StandardCharsets.UTF_8));

        assertDoesNotThrow(() -> callback.accept(publish));

        verifyNoInteractions(messageService);
    }

    @Test
    void callback_nullBytesPayload_handlesGracefully() {
        service.subscribe();
        verify(complete).callback(callbackCaptor.capture());
        Consumer<Mqtt5Publish> callback = callbackCaptor.getValue();

        Mqtt5Publish publish = mock(Mqtt5Publish.class);
        when(publish.getPayloadAsBytes()).thenReturn(new byte[0]);

        assertDoesNotThrow(() -> callback.accept(publish));

        verifyNoInteractions(messageService);
    }
}