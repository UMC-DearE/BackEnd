package com.deare.backend.api.report.dto.result;

import com.deare.backend.domain.report.entity.enums.ReanalyzeReason;

public record Reanalyze(
        boolean enabled,
        ReanalyzeReason reason,
        String message
) {
    public static Reanalyze reanalyzedEnabled() {
        return new Reanalyze(true, null, null);
    }

    public static Reanalyze reanalyzedDisabled(ReanalyzeReason reason, String message) {
        return new Reanalyze(false, reason, message);
    }
}
