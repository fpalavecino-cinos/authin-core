package org.cinos.authin_core.posts.repository;

import org.cinos.authin_core.posts.entity.PostImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImageEntity, Long> {
}
