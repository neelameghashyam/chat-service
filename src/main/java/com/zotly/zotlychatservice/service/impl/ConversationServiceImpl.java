package com.zotly.zotlychatservice.service.impl;

import com.zotly.zotlychatservice.dto.ConversationDTO;
import com.zotly.zotlychatservice.entity.Conversation;
import com.zotly.zotlychatservice.entity.User;
import com.zotly.zotlychatservice.mapper.ConversationMapper;
import com.zotly.zotlychatservice.repository.ConversationRepository;
import com.zotly.zotlychatservice.repository.UserRepository;
import com.zotly.zotlychatservice.service.ConversationService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public ConversationServiceImpl(ConversationRepository conversationRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Conversation createConversation(ConversationDTO conversationDTO) {
        User user = userRepository.findById(conversationDTO.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        User agent = userRepository.findById(conversationDTO.agentId())
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        if (conversationRepository.findByChatToken(conversationDTO.chatToken()) != null) {
            throw new RuntimeException("Chat token already exists");
        }

        Conversation conversation = ConversationMapper.toEntity(conversationDTO);
        conversation.setUser(user);
        conversation.setAgent(agent);
        if (conversation.getChatToken() == null) {
            conversation.setChatToken(UUID.randomUUID().toString());
        }
        if (conversation.getTimestamp() == null) {
            conversation.setTimestamp(java.time.LocalDateTime.now());
        }
        if (conversation.getArchived() == null) {
            conversation.setArchived(false);
        }
        if (conversation.getLikeCount() == null) {
            conversation.setLikeCount(0);
        }
        if (conversation.getDislikeCount() == null) {
            conversation.setDislikeCount(0);
        }
        if (conversation.getIsUnread() == null) {
            conversation.setIsUnread(true);
        }
        return conversationRepository.save(conversation);
    }
}