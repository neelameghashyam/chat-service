package com.zotly.zotlychatservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zotly.zotlychatservice.entity.Conversation;
import com.zotly.zotlychatservice.repository.ConversationRepository;
import com.zotly.zotlychatservice.service.MqttPublisherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ConversationControllerTest {

    @Mock
    private ConversationRepository repository;

    @Mock
    private MqttPublisherService mqttPublisher;

    @InjectMocks
    private ConversationController controller;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void getAllConversations_success() throws Exception {
        Conversation conv1 = Conversation.builder().id(1L).build();
        Conversation conv2 = Conversation.builder().id(2L).build();

        when(repository.findAll()).thenReturn(Arrays.asList(conv1, conv2));

        mockMvc.perform(get("/api/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(repository).findAll();
    }

    @Test
    void getConversationById_success() throws Exception {
        Conversation conv = Conversation.builder().id(1L).build();

        when(repository.findById(1L)).thenReturn(Optional.of(conv));

        mockMvc.perform(get("/api/conversations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(repository).findById(1L);
    }

    // @Test
    // void getConversationById_notFound() throws Exception {
    //     when(repository.findById(1L)).thenReturn(Optional.empty());

    //     mockMvc.perform(get("/api/conversations/1"))
    //             .andExpect(status().isInternalServerError()); // Since it throws RuntimeException

    //     verify(repository).findById(1L);
    // }

    @Test
    void createConversation_success() throws Exception {
        Conversation request = Conversation.builder().build();
        Conversation saved = Conversation.builder().id(1L).build();

        when(repository.save(ArgumentMatchers.any(Conversation.class))).thenReturn(saved);
        doNothing().when(mqttPublisher).publish(anyString(), any());

        mockMvc.perform(post("/api/conversations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(repository).save(ArgumentMatchers.any(Conversation.class));
        verify(mqttPublisher).publish(eq("zotly/conversations/1"), eq(saved));
    }

    @Test
    void updateStatus_success() throws Exception {
        Conversation conv = Conversation.builder().id(1L).status(Conversation.Status.ACTIVE).build();
        Conversation updated = Conversation.builder().id(1L).status(Conversation.Status.CLOSED).build();

        when(repository.findById(1L)).thenReturn(Optional.of(conv));
        when(repository.save(ArgumentMatchers.any(Conversation.class))).thenReturn(updated);
        doNothing().when(mqttPublisher).publish(anyString(), any());

        mockMvc.perform(put("/api/conversations/1/status")
                .param("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        verify(repository).findById(1L);
        verify(repository).save(ArgumentMatchers.any(Conversation.class));
        verify(mqttPublisher).publish(eq("zotly/conversations/1/status"), eq(updated));
    }

    // @Test
    // void updateStatus_notFound() throws Exception {
    //     when(repository.findById(1L)).thenReturn(Optional.empty());

    //     mockMvc.perform(put("/api/conversations/1/status")
    //             .param("status", "CLOSED"))
    //             .andExpect(status().isInternalServerError());

    //     verify(repository).findById(1L);
    // }

    @Test
    void deleteConversation_success() throws Exception {
        doNothing().when(repository).deleteById(1L);
        doNothing().when(mqttPublisher).publish(anyString(), anyString());

        mockMvc.perform(delete("/api/conversations/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Conversation deleted with id 1"));

        verify(repository).deleteById(1L);
        verify(mqttPublisher).publish(eq("zotly/conversations/deleted/1"), eq("Conversation deleted"));
    }
}