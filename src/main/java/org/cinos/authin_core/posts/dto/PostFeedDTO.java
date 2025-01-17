package org.cinos.authin_core.posts.dto;

import org.cinos.authin_core.posts.models.CurrencySymbol;
import lombok.Builder;

import java.util.List;

@Builder
public record PostFeedDTO(
        Long id,
        String make,
        String model,
        String year,
        Double price,
        CurrencySymbol currencySymbol,
        Boolean isUsed,
        String userFullName,
        String dateTimeValue,
        String ubication,
        List<String> imagesUrls
) {
}
