package com.deare.backend.api.report.dto.response;

import com.deare.backend.domain.report.entity.ReportAnalysis;
import com.deare.backend.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public record ReportReanalyzeResponseDTO(
        String title,
        String description,
        List<String> hashtags,
        LocalDateTime analyzedAt
) {
    public static ReportReanalyzeResponseDTO of(User user, ReportAnalysis analysis) {
        return new ReportReanalyzeResponseDTO(
                "TO, " + user.getNickname(),
                analysis.getDescription(),
                List.of(analysis.getHashtag1(), analysis.getHashtag2()),
                analysis.getAnalyzedAt()
        );
    }
}
