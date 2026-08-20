package com.deare.backend.api.report.service;

import com.deare.backend.api.report.dto.response.ReportResponseDTO;
import com.deare.backend.api.report.dto.result.FromRanking;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.letter.repository.query.dto.FromLetterRankingProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final LetterRepository letterRepository;

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDTO getReport(Long userId) {
        List<FromRanking> fromRanking = getTopFromRanking(userId);

        return ReportResponseDTO.builder()
                .fromRanking(fromRanking)
                .analysis(null)   // TODO: AI 분석 붙을 때 채우기
                .reanalyze(null)  // TODO: AI 분석 붙을 때 채우기
                .build();
    }

    private List<FromRanking> getTopFromRanking(Long userId) {
        List<FromLetterRankingProjection> topFroms = letterRepository.findTopFromsByLetterCount(userId);

        List<FromRanking> result = new ArrayList<>();
        for (int i = 0; i < topFroms.size(); i++) {
            FromLetterRankingProjection f = topFroms.get(i);
            result.add(FromRanking.of(
                    i + 1,
                    f.getName(),
                    f.getLetterCount().intValue(),
                    f.getBgColor(),
                    f.getFontColor()
            ));
        }
        return result;
    }
}