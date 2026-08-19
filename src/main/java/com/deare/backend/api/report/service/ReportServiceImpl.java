package com.deare.backend.api.report.service;

import com.deare.backend.api.report.dto.response.ReportResponseDTO;
import com.deare.backend.api.report.dto.result.FromRanking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService{

    private final ReportQueryService reportQueryService;

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDTO getReport(Long userId) {
        List<FromRanking> fromRanking = reportQueryService.getTopFromRanking(userId);

        return ReportResponseDTO.builder()
                .fromRanking(fromRanking)
                .analysis(null)   // TODO: AI 분석 붙을 때 채우기
                .reanalyze(null)  // TODO: AI 분석 붙을 때 채우기
                .build();
    }
}
