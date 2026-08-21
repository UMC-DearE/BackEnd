package com.deare.backend.api.report.service;

import com.deare.backend.api.report.dto.response.ReportReanalyzeResponseDTO;
import com.deare.backend.api.report.dto.response.ReportResponseDTO;

public interface ReportService {
    ReportResponseDTO getReport(Long userId);
    ReportReanalyzeResponseDTO reanalyze(Long userId);
}
