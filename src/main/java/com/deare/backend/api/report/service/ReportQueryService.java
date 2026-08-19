package com.deare.backend.api.report.service;

import com.deare.backend.api.report.dto.result.FromRanking;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.letter.repository.query.dto.FromLetterRankingProjection;
import com.deare.backend.domain.report.entity.ReportAnalysis;
import com.deare.backend.domain.report.repository.ReportAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportQueryService {

    private final LetterRepository letterRepository;

    public List<FromRanking> getTopFromRanking(Long userId) {
        List<FromLetterRankingProjection> topFroms = letterRepository.findTopFromsByLetterCount(userId);

        List<FromRanking> result = new ArrayList<>();
        for (int i = 0; i < topFroms.size(); i++) {
            FromLetterRankingProjection from = topFroms.get(i);
            result.add(FromRanking.of(
                    i + 1,
                    from.getName(),
                    from.getLetterCount().intValue(),
                    from.getBgColor(),
                    from.getFontColor()
            ));
        }
        return result;
    }
}
