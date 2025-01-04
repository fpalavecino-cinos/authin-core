package org.cinos.authin_core.users.dto;

import lombok.Builder;

@Builder
public record AccountDTO(
        Long id,
        String name,
        String username,
        String lastname,
        String email,
        Integer points,
        Long followers,
        Long followings,
        String avatarImg) {
}
