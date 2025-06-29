package org.cinos.core.messages.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ConversationDTO(
        Long id,
        LocalDateTime lastUpdated,
        String lastMessage,
        Long receiverId,
        String receiverName,
        String receiverAvatar) {}
