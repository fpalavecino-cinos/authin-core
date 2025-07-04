package org.cinos.core.posts.dto;

import org.cinos.core.posts.models.CurrencySymbol;
import lombok.Builder;

import java.time.LocalDateTime;
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
        Long userId,
        LocalDateTime publicationDate,
        PostLocationDTO location,
        String kilometers,
        List<String> imagesUrls,
        Boolean isVerified,
        Boolean isApproved
) {
}
