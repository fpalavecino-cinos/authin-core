package org.cinos.authin_core.posts.controller.request;

import org.cinos.authin_core.posts.dto.PostLocationDTO;
import org.cinos.authin_core.posts.models.CurrencySymbol;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record PostCreateRequest(
        String make,
        String model,
        String year,
        Boolean isUsed,
        Double price,
        String description,
        String fuel,
        String transmission,
        Long userId,
        Boolean active,
        String kilometers,
        PostLocationDTO location,
        CurrencySymbol currencySymbol) implements Serializable {
}
