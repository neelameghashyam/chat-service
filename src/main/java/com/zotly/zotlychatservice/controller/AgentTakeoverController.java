package com.zotly.zotlychatservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.zotly.zotlychatservice.entity.AgentTakeover;
import com.zotly.zotlychatservice.service.AgentTakeoverService;
import com.zotly.zotlychatservice.service.MqttPublisherService;

import java.util.List;

@RestController
@RequestMapping("/api/agent-takeovers")
public class AgentTakeoverController {

    @Autowired
    private AgentTakeoverService service;

    @Autowired
    private MqttPublisherService mqttPublisher;

    @PostMapping
    public ResponseEntity<AgentTakeover> createTakeover(@RequestBody AgentTakeover request) {
        if(request.getConversationId() == null || request.getToAgent() == null) {
            return ResponseEntity.badRequest().build();
        }
        AgentTakeover saved = service.save(request);
        mqttPublisher.publish("zotly/agent-takeovers/" + saved.getTakeoverId(), saved);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<AgentTakeover>> getAllTakeovers() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgentTakeover> getTakeoverById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgentTakeover> updateTakeover(@PathVariable Long id, @RequestBody AgentTakeover request) {
        try {
            AgentTakeover updated = service.update(id, request);
            mqttPublisher.publish("zotly/agent-takeovers/" + updated.getTakeoverId(), updated);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTakeover(@PathVariable Long id) {
        service.delete(id);
        mqttPublisher.publish("zotly/agent-takeovers/deleted/" + id, "Takeover deleted");
        return ResponseEntity.noContent().build();
    }
}
