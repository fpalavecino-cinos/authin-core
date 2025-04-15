package org.cinos.authin_core.posts.dto;

import lombok.Builder;
@Builder
public record PostProfileDTO(Long id, String firstImage) {
}
