package org.cinos.authin_core.posts.dto;
import lombok.Builder;
import org.cinos.authin_core.technical_verification.dto.TechnicalVerificationDTO;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PostDTO(
        Long id,
        String make,
        String model,
        String year,
        Double price,
        Boolean isUsed,
        Long userId,
        LocalDateTime publicationDate,
        String userFullName,
        Boolean active,
        String currencySymbol,
        String kilometers,
        String fuel,
        String transmission,
        PostLocationDTO location,
        List<String> imagesUrls,
        String userAvatar,
        TechnicalVerificationDTO technicalVerification,
        Boolean isVerified) {
}
