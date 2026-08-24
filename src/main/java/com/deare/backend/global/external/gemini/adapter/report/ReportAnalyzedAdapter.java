package com.deare.backend.global.external.gemini.adapter.report;

import com.deare.backend.global.external.gemini.dto.response.report.ReportAnalyzeResponseDTO;

import java.util.List;

public interface ReportAnalyzedAdapter {
    ReportAnalyzeResponseDTO reportAnalyze(List<String> summaries);
}
