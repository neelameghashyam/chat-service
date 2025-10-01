package com.zotly.zotlychatservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zotly.zotlychatservice.dto.MessageDTO;
import com.zotly.zotlychatservice.service.MessageService;
import com.zotly.zotlychatservice.service.MqttPublisherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @Mock
    private MessageService service;

    @Mock
    private MqttPublisherService mqttPublisher;

    @InjectMocks
    private MessageController controller;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAll_success() throws Exception {
        MessageDTO dto1 = new MessageDTO(1L, 1L, "USER", 1L, "Hello", "TEXT");
        MessageDTO dto2 = new MessageDTO(2L, 1L, "AGENT", 2L, "Hi", "TEXT");

        when(service.getAll()).thenReturn(Arrays.asList(dto1, dto2));

        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].messageId").value(1L));

        verify(service).getAll();
    }

    @Test
    void getById_success() throws Exception {
        MessageDTO dto = new MessageDTO(1L, 1L, "USER", 1L, "Hello", "TEXT");

        when(service.getById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/messages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value(1L));

        verify(service).getById(1L);
    }

    @Test
    void create_success() throws Exception {
        MessageDTO request = new MessageDTO(null, 1L, "USER", 1L, "Hello", "TEXT");
        MessageDTO saved = new MessageDTO(1L, 1L, "USER", 1L, "Hello", "TEXT");

        when(service.create(any(MessageDTO.class))).thenReturn(saved);
        doNothing().when(mqttPublisher).publish(anyString(), any());

        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value(1L));

        verify(service).create(any(MessageDTO.class));
        verify(mqttPublisher).publish(eq("zotly/messages/1"), eq(saved));
    }

    @Test
    void create_validationFailure() throws Exception {
        MessageDTO invalid = new MessageDTO(null, null, "", 1L, "Hello", "TEXT");

        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void update_success() throws Exception {
        MessageDTO request = new MessageDTO(1L, 1L, "USER", 1L, "Updated", "TEXT");
        MessageDTO updated = new MessageDTO(1L, 1L, "USER", 1L, "Updated", "TEXT");

        when(service.update(eq(1L), any(MessageDTO.class))).thenReturn(updated);
        doNothing().when(mqttPublisher).publish(anyString(), any());

        mockMvc.perform(put("/api/messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated"));

        verify(service).update(eq(1L), any(MessageDTO.class));
        verify(mqttPublisher).publish(eq("zotly/messages/1"), eq(updated));
    }

    @Test
    void update_validationFailure() throws Exception {
        MessageDTO invalid = new MessageDTO(1L, null, "", 1L, "Updated", "TEXT");

        mockMvc.perform(put("/api/messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void delete_success() throws Exception {
        doNothing().when(service).delete(1L);
        doNothing().when(mqttPublisher).publish(anyString(), anyString());

        mockMvc.perform(delete("/api/messages/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
        verify(mqttPublisher).publish(eq("zotly/messages/deleted/1"), eq("Message deleted"));
    }
}