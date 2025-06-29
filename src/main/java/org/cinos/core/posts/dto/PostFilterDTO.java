package org.cinos.core.posts.dto;

import java.util.List;

public record PostFilterDTO(
        List<String> make,
        String model,
        String minYear,
        String maxYear,
        String fuelType,
        String transmission,
        Double minPrice,
        Double maxPrice,
        Integer minMileage,
        Integer maxMileage,
        Boolean isUsed,
        Integer page,
        Integer size
) {
}
