package com.zotly.zotlychatservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zotly.zotlychatservice.entity.Conversation;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByStatus(String status);
    List<Conversation> findByCustomerId(Long customerId);
}