package org.cinos.authin_core.posts.dto;

import org.cinos.authin_core.posts.entity.CommentEntity;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CommentDTO(Long id, Long postId, Long userId, String content, LocalDateTime dateTime) {
    public static CommentDTO toDTO(CommentEntity commentEntity) {
        return CommentDTO.builder()
                .id(commentEntity.getId())
                .postId(commentEntity.getPostId())
                .userId(commentEntity.getUserId())
                .content(commentEntity.getContent())
                .dateTime(commentEntity.getDateTime())
                .build();
    }

    public static CommentEntity toEntity(CommentDTO commentDTO) {
        return CommentEntity.builder()
                .id(commentDTO.id())
                .userId(commentDTO.userId())
                .postId(commentDTO.postId())
                .content(commentDTO.content())
                .dateTime(commentDTO.dateTime())
                .build();
    }
}
