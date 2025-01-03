package com.argctech.core.posts.dto;

import com.argctech.core.posts.models.CurrencySymbol;
import lombok.Builder;
import java.util.List;

@Builder
public record PostFeedDTO(
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
