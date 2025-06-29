package org.cinos.core.stripe.dto;

import lombok.Builder;

@Builder
public record SubscriptionResponse(
        String clientSecret,
        String message,
        Boolean success
) {
}