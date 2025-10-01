package com.zotly.zotlychatservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zotly.zotlychatservice.entity.AgentTakeover;
import com.zotly.zotlychatservice.service.AgentTakeoverService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AgentTakeoverControllerTest {

    @Mock
    private AgentTakeoverService service;

    @Mock
    private MqttPublisherService mqttPublisher;

    @InjectMocks
    private AgentTakeoverController controller;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void createTakeover_success() throws Exception {
        AgentTakeover request = new AgentTakeover();
        request.setConversationId(1L);
        request.setToAgent(2L);
        AgentTakeover saved = new AgentTakeover();
        saved.setTakeoverId(1L);
        saved.setConversationId(1L);
        saved.setToAgent(2L);

        when(service.save(any(AgentTakeover.class))).thenReturn(saved);
        doNothing().when(mqttPublisher).publish(anyString(), any());

        mockMvc.perform(post("/api/agent-takeovers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.takeoverId").value(1L));

        verify(service).save(any(AgentTakeover.class));
        verify(mqttPublisher).publish(eq("zotly/agent-takeovers/1"), eq(saved));
    }

    @Test
    void createTakeover_badRequest_missingFields() throws Exception {
        AgentTakeover request = new AgentTakeover();

        mockMvc.perform(post("/api/agent-takeovers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
        verifyNoInteractions(mqttPublisher);
    }

    @Test
    void getAllTakeovers_success() throws Exception {
        AgentTakeover takeover1 = new AgentTakeover();
        takeover1.setTakeoverId(1L);
        AgentTakeover takeover2 = new AgentTakeover();
        takeover2.setTakeoverId(2L);

        when(service.findAll()).thenReturn(Arrays.asList(takeover1, takeover2));

        mockMvc.perform(get("/api/agent-takeovers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].takeoverId").value(1L))
                .andExpect(jsonPath("$[1].takeoverId").value(2L));

        verify(service).findAll();
    }

    @Test
    void getTakeoverById_success() throws Exception {
        AgentTakeover takeover = new AgentTakeover();
        takeover.setTakeoverId(1L);

        when(service.findById(1L)).thenReturn(Optional.of(takeover));

        mockMvc.perform(get("/api/agent-takeovers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.takeoverId").value(1L));

        verify(service).findById(1L);
    }

    @Test
    void getTakeoverById_notFound() throws Exception {
        when(service.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/agent-takeovers/1"))
                .andExpect(status().isNotFound());

        verify(service).findById(1L);
    }

    @Test
    void updateTakeover_success() throws Exception {
        AgentTakeover request = new AgentTakeover();
        request.setConversationId(1L);
        request.setToAgent(3L);
        AgentTakeover updated = new AgentTakeover();
        updated.setTakeoverId(1L);
        updated.setConversationId(1L);
        updated.setToAgent(3L);

        when(service.update(eq(1L), any(AgentTakeover.class))).thenReturn(updated);
        doNothing().when(mqttPublisher).publish(anyString(), any());

        mockMvc.perform(put("/api/agent-takeovers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.takeoverId").value(1L))
                .andExpect(jsonPath("$.toAgent").value(3L));

        verify(service).update(eq(1L), any(AgentTakeover.class));
        verify(mqttPublisher).publish(eq("zotly/agent-takeovers/1"), eq(updated));
    }

    @Test
    void updateTakeover_notFound() throws Exception {
        AgentTakeover request = new AgentTakeover();

        when(service.update(eq(1L), any(AgentTakeover.class))).thenThrow(new RuntimeException());

        mockMvc.perform(put("/api/agent-takeovers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(service).update(eq(1L), any(AgentTakeover.class));
        verifyNoInteractions(mqttPublisher);
    }

    @Test
    void deleteTakeover_success() throws Exception {
        doNothing().when(service).delete(1L);
        doNothing().when(mqttPublisher).publish(anyString(), anyString());

        mockMvc.perform(delete("/api/agent-takeovers/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
        verify(mqttPublisher).publish(eq("zotly/agent-takeovers/deleted/1"), eq("Takeover deleted"));
    }
}