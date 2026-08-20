package com.deare.backend.api.report.dto.response;

import com.deare.backend.api.report.dto.result.Analysis;
import com.deare.backend.api.report.dto.result.FromRanking;
import com.deare.backend.api.report.dto.result.Reanalyze;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReportResponseDTO {
    private List<FromRanking> fromRanking;
    private Analysis analysis;
    private Reanalyze reanalyze;
}
