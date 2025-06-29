package org.cinos.core.messages.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MessageDTO(
        Long id,
        Long senderId,
        Long recipientId,
        String content,
        LocalDateTime timestamp
) {}