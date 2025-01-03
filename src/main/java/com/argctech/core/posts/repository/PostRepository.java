package com.argctech.core.posts.repository;

import com.argctech.core.posts.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Long>, JpaSpecificationExecutor<PostEntity> {

    <T> List<T> findByUserId(Long userId, Class<T> type);
    <T> Page<T> findAllByUserIdInOrderByPublicationDateDesc(List<Long> usersId, Class<T> type, Pageable pageable);
    <T> Page<T> findAllByUserIdInAndPublicationDateAfterOrderByPublicationDateDesc(List<Long> usersId, LocalDateTime date, Class<T> type, Pageable pageable);
    <T> Page<T> findAllByPublicationDateAfterOrderByPublicationDateDesc(LocalDateTime date, Class<T> type, Pageable pageable);
    <T> Page<T> findAllByOrderByPublicationDateDesc(Class<T> type, Pageable pageable);
    <T> Optional<T> findById(Long id, Class<T> type);

}
