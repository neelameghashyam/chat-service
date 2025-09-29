package com.zotly.zotlychatservice.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.zotly.zotlychatservice.dto.MessageDTO;
import com.zotly.zotlychatservice.service.MessageService;
import com.zotly.zotlychatservice.service.MqttPublisherService;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService service;
    private final MqttPublisherService mqttPublisher;

    public MessageController(MessageService service, MqttPublisherService mqttPublisher) {
        this.service = service;
        this.mqttPublisher = mqttPublisher;
    }

    @GetMapping
    public List<MessageDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<MessageDTO> create(@Valid @RequestBody MessageDTO dto) {
        MessageDTO saved = service.create(dto);
        // Publish to MQTT topic
        mqttPublisher.publish("zotly/messages/" + saved.conversationId(), saved);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageDTO> update(@PathVariable Long id,
                                             @Valid @RequestBody MessageDTO dto) {
        MessageDTO updated = service.update(id, dto);
        mqttPublisher.publish("zotly/messages/" + updated.conversationId(), updated);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        mqttPublisher.publish("zotly/messages/deleted/" + id, "Message deleted");
        return ResponseEntity.noContent().build();
    }
}
