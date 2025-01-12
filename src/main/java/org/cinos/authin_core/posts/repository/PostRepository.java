package org.cinos.authin_core.posts.repository;

import org.cinos.authin_core.posts.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Long>, JpaSpecificationExecutor<PostEntity> {

    List<PostEntity> findByUserId(Long userId);
    List<PostEntity> findAllByUserId(Long userId);
    Page<PostEntity> findAllByUserIdInOrderByPublicationDateDesc(List<Long> usersId, Pageable pageable);
    <T> List<T>  findByUsersSaved_Id(Long userId);

}
