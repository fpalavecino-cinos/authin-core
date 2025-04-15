package org.cinos.authin_core.posts.repository;

import org.cinos.authin_core.posts.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Long>, JpaSpecificationExecutor<PostEntity> {

    List<PostEntity> findByUserAccount_Id(Long userId);
    List<PostEntity> findAllByUserAccount_IdAndActiveTrue(Long userId);
    Page<PostEntity> findAllByUserAccount_IdInOrderByPublicationDateDesc(List<Long> usersId, Pageable pageable);
    <T> List<T>  findByUsersSaved_Id(Long userId);

}
