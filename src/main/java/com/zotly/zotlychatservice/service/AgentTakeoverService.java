package com.zotly.zotlychatservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zotly.zotlychatservice.entity.AgentTakeover;
import com.zotly.zotlychatservice.repository.AgentTakeoverRepository;

import java.util.List;
import java.util.Optional;

@Service
public class AgentTakeoverService {

    @Autowired
    private AgentTakeoverRepository repository;

    // CREATE
    public AgentTakeover save(AgentTakeover takeover) {
        return repository.save(takeover);
    }

    // READ ALL
    public List<AgentTakeover> findAll() {
        return repository.findAll();
    }

    // READ BY ID
    public Optional<AgentTakeover> findById(Long id) {
        return repository.findById(id);
    }

    // UPDATE
    public AgentTakeover update(Long id, AgentTakeover updatedTakeover) {
        return repository.findById(id).map(existing -> {
            existing.setConversationId(updatedTakeover.getConversationId());
            existing.setFromAgent(updatedTakeover.getFromAgent());
            existing.setToAgent(updatedTakeover.getToAgent());
            existing.setTakeoverTime(updatedTakeover.getTakeoverTime());
            return repository.save(existing);
        }).orElseThrow(() -> new RuntimeException("AgentTakeover not found with id " + id));
    }

    // DELETE
    public void delete(Long id) {
        repository.deleteById(id);
    }
}