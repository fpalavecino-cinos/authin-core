package org.cinos.core.technical_verification.repository;

import org.cinos.core.technical_verification.entity.TechnicalVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TechnicalVerificationRepository extends JpaRepository<TechnicalVerification, Long> {
    Optional<TechnicalVerification> findByPost_Id(Long postId);
}
