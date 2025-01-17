package org.cinos.authin_core.posts.repository;

import org.cinos.authin_core.posts.entity.PostLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLocationRepository extends JpaRepository<PostLocationEntity, Long> {
}
