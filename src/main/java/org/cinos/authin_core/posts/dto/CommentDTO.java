package org.cinos.authin_core.posts.dto;

import org.cinos.authin_core.posts.entity.CommentEntity;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CommentDTO(Long id, Long postId, Long userId, String content, LocalDateTime commentDate, String accountAvatar, String userName) { }
