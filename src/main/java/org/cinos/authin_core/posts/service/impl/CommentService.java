package org.cinos.authin_core.posts.service.impl;

import lombok.RequiredArgsConstructor;
import org.cinos.authin_core.posts.dto.CommentDTO;
import org.cinos.authin_core.posts.entity.CommentEntity;
import org.cinos.authin_core.posts.repository.CommentRepository;
import org.cinos.authin_core.posts.service.ICommentService;
import org.cinos.authin_core.users.service.impl.AccountService;
import org.cinos.authin_core.users.utils.exceptions.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService implements ICommentService {

    private final CommentRepository commentRepository;
    private final AccountService accountService;

    @Override
    public Page<CommentDTO> getCommentsByPostId(Long postId, Pageable page) {
        Page<CommentEntity> commentPage = commentRepository.findByPostId(postId, page);
        List<CommentDTO> commentDTOs = commentPage.getContent().stream()
                .map(commentEntity -> {
                    try {
                        return CommentDTO.builder()
                                .id(commentEntity.getId())
                                .postId(commentEntity.getPostId())
                                .userId(commentEntity.getUserId())
                                .content(commentEntity.getContent())
                                .commentDate(commentEntity.getCommentDate())
                                .accountAvatar(accountService.getUserAccount(commentEntity.getUserId()).avatarImg())
                                .userName(accountService.getUserAccount(commentEntity.getUserId()).name())
                                .build();
                    } catch (UserNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

            return new PageImpl<>(commentDTOs, page, commentPage.getTotalElements());
    }

    @Override
    public Integer getCommentsLength(Long postId) {
        return commentRepository.findByPostId(postId).size();
    }

    @Override
    public CommentDTO createComment(CommentDTO commentDTO) {
        CommentEntity commentEntity = CommentEntity.builder()
                .postId(commentDTO.postId())
                .userId(commentDTO.userId())
                .content(commentDTO.content())
                .commentDate(LocalDateTime.now())
                .build();

        commentRepository.save(commentEntity);
        return CommentDTO.builder()
                .id(commentEntity.getId())
                .postId(commentEntity.getPostId())
                .userId(commentEntity.getUserId())
                .content(commentEntity.getContent())
                .commentDate(commentEntity.getCommentDate())
                .build();
    }
}
