package org.cinos.authin_core.posts.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PostLocationDTO(
        BigDecimal lat,
        BigDecimal lng
) {
}
