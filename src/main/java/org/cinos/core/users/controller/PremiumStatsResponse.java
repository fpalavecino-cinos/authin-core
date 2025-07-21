package org.cinos.core.users.controller;

import java.time.LocalDateTime;

public record PremiumStatsResponse(
    int verificationsRemaining,
    int verificationReportsRemaining,
    int verificationsUsed,
    LocalDateTime nextResetDate
) {} 