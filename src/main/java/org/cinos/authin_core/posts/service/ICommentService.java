package org.cinos.authin_core.posts.service;

import org.cinos.authin_core.posts.dto.CommentDTO;
import org.cinos.authin_core.users.utils.exceptions.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICommentService {
    Page<CommentDTO> getCommentsByPostId(Long postId, Pageable page);
    Integer getCommentsLength(Long postId);
    CommentDTO createComment(CommentDTO commentDTO) throws UserNotFoundException;

}
