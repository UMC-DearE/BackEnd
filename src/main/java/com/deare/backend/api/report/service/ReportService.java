package com.deare.backend.api.report.service;

import com.deare.backend.api.report.dto.response.ReportResponseDTO;
import com.deare.backend.api.report.dto.result.FromRanking;

public interface ReportService {
    ReportResponseDTO getReport(Long userId);
}
