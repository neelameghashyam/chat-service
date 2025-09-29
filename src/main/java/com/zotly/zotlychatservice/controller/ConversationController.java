package com.zotly.zotlychatservice.controller;

import org.springframework.web.bind.annotation.*;

import com.zotly.zotlychatservice.entity.Conversation;
import com.zotly.zotlychatservice.repository.ConversationRepository;
import com.zotly.zotlychatservice.service.MqttPublisherService;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationRepository conversationRepository;
    private final MqttPublisherService mqttPublisher;

    public ConversationController(ConversationRepository conversationRepository,
                                  MqttPublisherService mqttPublisher) {
        this.conversationRepository = conversationRepository;
        this.mqttPublisher = mqttPublisher;
    }

    @GetMapping
    public List<Conversation> getAllConversations() {
        return conversationRepository.findAll();
    }

    @GetMapping("/{id}")
    public Conversation getConversationById(@PathVariable Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id " + id));
    }

    @PostMapping
    public Conversation createConversation(@RequestBody Conversation conversation) {
        Conversation saved = conversationRepository.save(conversation);
        mqttPublisher.publish("zotly/conversations/" + saved.getId(), saved);
        return saved;
    }

    @PutMapping("/{id}/status")
    public Conversation updateStatus(@PathVariable Long id, @RequestParam Conversation.Status status) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id " + id));
        conversation.setStatus(status);
        Conversation updated = conversationRepository.save(conversation);
        mqttPublisher.publish("zotly/conversations/" + updated.getId() + "/status", updated);
        return updated;
    }

    @DeleteMapping("/{id}")
    public String deleteConversation(@PathVariable Long id) {
        conversationRepository.deleteById(id);
        mqttPublisher.publish("zotly/conversations/deleted/" + id, "Conversation deleted");
        return "Conversation deleted with id " + id;
    }
}
