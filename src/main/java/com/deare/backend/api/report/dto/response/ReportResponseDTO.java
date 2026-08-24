package com.deare.backend.api.report.dto.response;

import com.deare.backend.api.report.dto.result.Analysis;
import com.deare.backend.api.report.dto.result.FromRanking;
import com.deare.backend.api.report.dto.result.Reanalyze;

import java.util.List;

public record ReportResponseDTO(
        long totalLetterCount,
        List<FromRanking> fromRanking,
        Analysis analysis,
        Reanalyze reanalyze
) {
    public static ReportResponseDTO of(
            long totalLetterCount,
            List<FromRanking> fromRanking,
            Analysis analysis,
            Reanalyze reanalyze
    ) {
        return new ReportResponseDTO(totalLetterCount, fromRanking, analysis, reanalyze);
    }
}
