package com.deare.backend.api.report.service;

import com.deare.backend.api.report.dto.response.ReportResponseDTO;
import com.deare.backend.api.report.dto.result.Analysis;
import com.deare.backend.api.report.dto.result.FromRanking;
import com.deare.backend.api.report.dto.result.Reanalyze;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.letter.repository.query.dto.FromLetterRankingProjection;
import com.deare.backend.domain.report.entity.ReportAnalysis;
import com.deare.backend.domain.report.entity.enums.ReanalyzeReason;
import com.deare.backend.domain.report.exception.ReportErrorCode;
import com.deare.backend.domain.report.repository.ReportAnalysisRepository;
import com.deare.backend.domain.report.repository.ReportRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import com.deare.backend.global.external.gemini.adapter.report.ReportAnalyzedAdapter;
import com.deare.backend.global.external.gemini.dto.response.report.ReportAnalyzeResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final int MIN_LETTER_COUNT = 3;
    private static final int MIN_NEW_LETTER_COUNT = 3;
    private static final int AI_SUMMARY_LIMIT = 50;

    private final UserRepository userRepository;
    private final LetterRepository letterRepository;
    private final ReportAnalysisRepository reportAnalysisRepository;
    private final ReportAnalyzedAdapter reportAnalyzedAdapter;

    @Override
    @Transactional
    public ReportResponseDTO getReport(Long userId) {
        User user = getUser(userId);
        List<FromRanking> fromRanking = getTopFromRanking(userId);
        long totalCount = countVisibleLetters(userId);

        if (totalCount == 0) {
            return build(fromRanking, Analysis.noLetter(), Reanalyze.reanalyzedDisabled(null, null));
        }
        if (totalCount < MIN_LETTER_COUNT) {
            return build(fromRanking, Analysis.noEnoughLetters(), Reanalyze.reanalyzedDisabled(null, null));
        }

        Optional<ReportAnalysis> existing = findAnalysis(userId);

        // ── AI 호출 지점: 분석 이력이 없을 때만 ──
        if (existing.isEmpty()) {
            ReportAnalysis created = createAnalysis(user, totalCount);
            return build(fromRanking, Analysis.of(user, created),
                    Reanalyze.reanalyzedDisabled(ReanalyzeReason.JUST_ANALYZED, "방금 분석을 완료했어요"));
        }

        ReportAnalysis analysis = existing.get();
        return build(fromRanking, Analysis.of(user, analysis), evaluateReanalyze(userId, analysis));
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

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ReportErrorCode.REPORT_NOT_FOUND_USER));
    }

    private long countVisibleLetters(Long userId) {
        return letterRepository.countVisibleLettersByUser(userId, LocalDateTime.now());
    }

    private long countNewLettersSince(Long userId, LocalDateTime since) {
        return letterRepository.countVisibleLettersByUserSince(userId, since);
    }

    private Optional<ReportAnalysis> findAnalysis(Long userId) {
        return reportAnalysisRepository.findByUserId(userId);
    }

    private ReportAnalysis createAnalysis(User user, long totalLetterCount) {
        try {
            List<String> summaries = letterRepository.findAiSummariesByUser(user.getId(), AI_SUMMARY_LIMIT);
            ReportAnalyzeResponseDTO result = reportAnalyzedAdapter.reportAnalyze(summaries);

            ReportAnalysis analysis = new ReportAnalysis(
                    user,
                    result.getDescription(),
                    result.getHashtag1(),
                    result.getHashtag2(),
                    (int) totalLetterCount,
                    LocalDateTime.now()
            );
            return reportAnalysisRepository.save(analysis);
        } catch (Exception e) {
            throw new GeneralException(ReportErrorCode.REPORT_ANALYSIS_FAILED);
        }
    }

    private Reanalyze evaluateReanalyze(Long userId, ReportAnalysis analysis) {
        LocalDateTime lastAnalyzedAt = analysis.getAnalyzedAt();
        long newLetterCount = countNewLettersSince(userId, lastAnalyzedAt);
        boolean hasEnoughNewLetters = newLetterCount >= MIN_NEW_LETTER_COUNT;

        LocalDateTime thisMonday = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        boolean usedThisWeek = !lastAnalyzedAt.isBefore(thisMonday);

        if (hasEnoughNewLetters && !usedThisWeek) {
            return Reanalyze.reanalyzedEnabled();
        }
        if (usedThisWeek) {
            return Reanalyze.reanalyzedDisabled(ReanalyzeReason.WEEKLY_LIMIT, "다시 분석은 매주 월요일에 초기화돼요");
        }
        return Reanalyze.reanalyzedDisabled(ReanalyzeReason.NOT_ENOUGH_NEW_LETTERS, "새로운 편지가 3통 더 필요해요");
    }

    private ReportResponseDTO build(List<FromRanking> fromRanking, Analysis analysis, Reanalyze reanalyze) {
        return ReportResponseDTO.builder()
                .fromRanking(fromRanking)
                .analysis(analysis)
                .reanalyze(reanalyze)
                .build();
    }
}