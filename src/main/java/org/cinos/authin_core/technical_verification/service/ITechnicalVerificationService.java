package org.cinos.authin_core.technical_verification.service;

import org.cinos.authin_core.technical_verification.dto.TechnicalVerificationRequest;
import org.cinos.authin_core.posts.utils.exceptions.PostNotFoundException;

import java.time.LocalDateTime;

public interface ITechnicalVerificationService {
    void orderVerification(Long postId) throws PostNotFoundException;
    void acceptVerification(Long postId, LocalDateTime verificationAppointment) throws PostNotFoundException;
    void processVerification(TechnicalVerificationRequest request) throws PostNotFoundException;
}
