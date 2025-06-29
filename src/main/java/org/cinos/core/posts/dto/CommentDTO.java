package org.cinos.core.posts.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CommentDTO(Long id, Long postId, Long userId, String content, LocalDateTime commentDate, String accountAvatar, String userName) { }
