package com.argctech.core.auth.controller.response;

import lombok.Builder;

@Builder
public record LoginResponse(String username, String email, String name, String lastname, String role, String token) {
}
