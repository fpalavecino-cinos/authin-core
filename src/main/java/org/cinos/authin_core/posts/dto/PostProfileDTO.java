package org.cinos.authin_core.posts.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record PostProfileDTO(Long id, String firstImage) {
}
