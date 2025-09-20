package com.zotly.zotlychatservice.service;

import com.zotly.zotlychatservice.dto.ConversationDTO;
import com.zotly.zotlychatservice.entity.Conversation;

public interface ConversationService {
    Conversation createConversation(ConversationDTO conversationDTO);
}