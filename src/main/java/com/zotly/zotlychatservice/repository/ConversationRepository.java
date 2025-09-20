package com.zotly.zotlychatservice.repository;

import com.zotly.zotlychatservice.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Conversation findByChatToken(String chatToken);
}