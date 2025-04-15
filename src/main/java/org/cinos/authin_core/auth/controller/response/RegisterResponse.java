package org.cinos.authin_core.auth.controller.response;

import lombok.Builder;

@Builder
public record RegisterResponse(Long id, String username, String email, String name, String role, String accessToken, String refreshToken) {
}
