package com.argctech.core.posts.dto;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record PostDTO(
        Long id,
        String make,
        String model,
        String year,
        Double price,
        Boolean isUsed,
        LocalDateTime publicationDate,
        Long userId,
        String description,
        Integer likes,
        Boolean active) {
}
