package org.cinos.core.users.repository;

import org.cinos.core.users.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    <T> Optional<T> findById(Long id, Class<T> type);
    Optional<UserEntity> findByUsername(String username);
    <T> Optional<T> findByUsername(String username, Class<T> type);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByStripeCustomerId(String stripeCustomerId);
    java.util.Optional<UserEntity> findByStripeSubscriptionId(String stripeSubscriptionId);
}
