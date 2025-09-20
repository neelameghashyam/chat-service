package com.zotly.zotlychatservice.dto;

import java.time.LocalDateTime;

public record ConversationDTO(
        Long userId,
        Long agentId,
        String message,
        LocalDateTime timestamp,
        Boolean archived,
        Integer likeCount,
        Integer dislikeCount,
        String comments,
        String chatToken,
        Boolean isUnread,
        String threadId
) {
}