package com.argctech.core.posts.controller.request;

import com.argctech.core.posts.models.CurrencySymbol;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;
import java.io.Serializable;
import java.util.List;

@Builder
public record PostCreateRequest(
        String make,
        String model,
        String year,
        Boolean isUsed,
        Double price,
        String description,
        Long userId,
        Boolean active,
        CurrencySymbol currencySymbol) implements Serializable {
}
