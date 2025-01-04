package org.cinos.authin_core.posts.repository;

import org.cinos.authin_core.posts.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
}
