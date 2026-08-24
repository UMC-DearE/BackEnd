package com.deare.backend.api.report.dto.result;

import com.deare.backend.domain.report.entity.ReportAnalysis;
import com.deare.backend.domain.report.entity.enums.AnalysisStatus;
import com.deare.backend.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public record Analysis(
        AnalysisStatus status,
        String title,
        String profileImageUrl,
        String description,
        List<String> hashtags,
        LocalDateTime analyzedAt
){
    public static Analysis noLetter() {
        return new Analysis(AnalysisStatus.NO_LETTER, null, null, null, null, null);
    }

    public static Analysis noEnoughLetters() {
        return new Analysis(AnalysisStatus.NOT_ENOUGH_LETTER, null, null, null, null, null);
    }

    public static Analysis of(User user, ReportAnalysis analysis) {
        return new Analysis(
                AnalysisStatus.AVAILABLE,
                "TO, " + user.getNickname(),
                user.getImage() != null ? user.getImage().getUrl() : null,
                analysis.getDescription(),
                List.of(analysis.getHashtag1(), analysis.getHashtag2()),
                analysis.getAnalyzedAt()
        );
    }
}