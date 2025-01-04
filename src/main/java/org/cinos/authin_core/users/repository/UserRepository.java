package org.cinos.authin_core.users.repository;

import org.cinos.authin_core.users.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    <T> Optional<T> findById(Long id, Class<T> type);
    Optional<UserEntity> findByUsername(String username);
    <T> Optional<T> findByUsername(String username, Class<T> type);
    String findNameAndLastnameById(Long id);
}
