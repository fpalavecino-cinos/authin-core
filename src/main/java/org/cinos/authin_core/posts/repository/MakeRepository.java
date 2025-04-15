package org.cinos.authin_core.posts.repository;

import org.cinos.authin_core.posts.entity.MakeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MakeRepository extends JpaRepository<MakeEntity, Long> {
    Optional<MakeEntity> findByName(String name);
    List<MakeEntity> findByNameContainingIgnoreCase(String name);

}
