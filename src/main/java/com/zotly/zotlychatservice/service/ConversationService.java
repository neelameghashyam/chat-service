package com.zotly.zotlychatservice.service;

import org.springframework.stereotype.Service;

import com.zotly.zotlychatservice.entity.Conversation;
import com.zotly.zotlychatservice.repository.ConversationRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {

    private final ConversationRepository repository;

    public ConversationService(ConversationRepository repository) {
        this.repository = repository;
    }

    // Create or update a conversation
    public Conversation save(Conversation conversation) {
        return repository.save(conversation);
    }

    // Get all conversations
    public List<Conversation> getAll() {
        return repository.findAll();
    }

    // Get conversation by ID
    public Optional<Conversation> getById(Long conversationId) {
        return repository.findById(conversationId);
    }

    // Get conversations by status
    public List<Conversation> getByStatus(String status) {
        return repository.findByStatus(status);
    }

    // Get conversations by customer
    public List<Conversation> getByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId);
    }
}