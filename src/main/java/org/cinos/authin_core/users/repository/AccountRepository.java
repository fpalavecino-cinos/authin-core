package org.cinos.authin_core.users.repository;

import org.cinos.authin_core.users.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    <T> Optional<T> findById(Long id, Class<T> type);
}
