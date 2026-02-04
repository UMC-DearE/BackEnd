package com.deare.backend.api.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReportStatsResponseDTO {
    private boolean isDummy;
    private List<Top3FromDTO> top3From;
    private List<String> topPhrases;
    private List<EmotionDistributionDTO> emotionDistribution;

}
