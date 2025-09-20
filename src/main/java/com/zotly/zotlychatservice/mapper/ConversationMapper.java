package com.zotly.zotlychatservice.mapper;

import com.zotly.zotlychatservice.dto.ConversationDTO;
import com.zotly.zotlychatservice.entity.Conversation;

public class ConversationMapper {

    public static ConversationDTO toDTO(Conversation conversation) {
        return new ConversationDTO(
                conversation.getUser().getId(),
                conversation.getAgent().getId(),
                conversation.getMessage(),
                conversation.getTimestamp(),
                conversation.getArchived(),
                conversation.getLikeCount(),
                conversation.getDislikeCount(),
                conversation.getComments(),
                conversation.getChatToken(),
                conversation.getIsUnread(),
                conversation.getThreadId()
        );
    }

    public static Conversation toEntity(ConversationDTO conversationDTO) {
        Conversation conversation = new Conversation();
        conversation.setMessage(conversationDTO.message());
        conversation.setTimestamp(conversationDTO.timestamp());
        conversation.setArchived(conversationDTO.archived());
        conversation.setLikeCount(conversationDTO.likeCount());
        conversation.setDislikeCount(conversationDTO.dislikeCount());
        conversation.setComments(conversationDTO.comments());
        conversation.setChatToken(conversationDTO.chatToken());
        conversation.setIsUnread(conversationDTO.isUnread());
        conversation.setThreadId(conversationDTO.threadId());
        return conversation;
    }
}