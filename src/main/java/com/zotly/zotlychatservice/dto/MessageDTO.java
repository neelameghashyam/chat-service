package com.zotly.zotlychatservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MessageDTO(
        Long messageId,
        @NotNull Long conversationId,
        @NotBlank String senderType,
        @NotNull Long senderId,
        @NotBlank String content,
        String messageType
) {}