package org.cinos.authin_core.posts.dto;

import lombok.Builder;

@Builder
public record AcceptVerificationRequest(
        Long postId,
        String appointmentDate
) {
}
