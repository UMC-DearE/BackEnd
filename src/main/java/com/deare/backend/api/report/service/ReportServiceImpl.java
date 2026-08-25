package com.deare.backend.api.report.service;

import com.deare.backend.api.report.dto.response.ReportReanalyzeResponseDTO;
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
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import com.deare.backend.global.common.exception.GeneralMessageException;
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
@Transactional(readOnly = true)
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
        long totalLetterCount = countVisibleLetters(userId);

        if (totalLetterCount == 0) {
            return build(
                    totalLetterCount,
                    fromRanking,
                    Analysis.noLetter(),
                    Reanalyze.reanalyzedDisabled(null, null)
            );
        }

        if (totalLetterCount < MIN_LETTER_COUNT) {
            return build(
                    totalLetterCount,
                    fromRanking,
                    Analysis.noEnoughLetters(),
                    Reanalyze.reanalyzedDisabled(null, null)
            );
        }

        ReportAnalysis analysis = findAnalysis(userId)
                .orElseGet(() -> createAnalysisSafely(userId, totalLetterCount));

        Reanalyze reanalyze = evaluateReanalyze(
                userId,
                analysis
        );

        return build(
                totalLetterCount,
                fromRanking,
                Analysis.of(user, analysis),
                reanalyze
        );
    }

    @Override
    @Transactional
    public ReportReanalyzeResponseDTO reanalyze(Long userId) {
        // 동일 사용자에 대한 재분석 요청을 직렬화해 중복 호출/덮어쓰기를 방지
        User user = getUserForUpdate(userId);

        long totalLetterCount = countVisibleLetters(userId);

        validateMinimumLetterCount(totalLetterCount);

        ReportAnalysis analysis = findAnalysis(userId)
                .orElse(null);

        if (analysis != null) {
            validateReanalyze(userId, analysis);
        }

        ReportAnalyzeResponseDTO result = analyze(userId);

        ReportAnalysis saved = saveAnalysis(
                user,
                analysis,
                result,
                totalLetterCount
        );

        return ReportReanalyzeResponseDTO.of(user, saved);
    }

    private ReportAnalysis createAnalysisSafely(
            Long userId,
            long totalLetterCount
    ) {
        User lockedUser = getUserForUpdate(userId);

        return findAnalysis(userId)
                .orElseGet(() -> createAnalysis(lockedUser, totalLetterCount));
    }

    private ReportAnalysis createAnalysis(
            User user,
            long totalLetterCount
    ) {
        ReportAnalyzeResponseDTO result = analyze(user.getId());

        return saveAnalysis(
                user,
                null,
                result,
                totalLetterCount
        );
    }

    private ReportAnalyzeResponseDTO analyze(Long userId) {
        List<String> summaries =
                letterRepository.findAiSummariesByUser(
                        userId,
                        AI_SUMMARY_LIMIT
                );

        return reportAnalyzedAdapter.reportAnalyze(summaries);
    }

    private ReportAnalysis saveAnalysis(
            User user,
            ReportAnalysis existing,
            ReportAnalyzeResponseDTO result,
            long totalLetterCount
    ) {
        LocalDateTime now = LocalDateTime.now();

        if (existing == null) {
            return reportAnalysisRepository.save(
                    new ReportAnalysis(
                            user,
                            result.getDescription(),
                            result.getHashtag1(),
                            result.getHashtag2(),
                            (int) totalLetterCount,
                            now
                    )
            );
        }

        existing.reanalyze(
                result.getDescription(),
                result.getHashtag1(),
                result.getHashtag2(),
                (int) totalLetterCount,
                now
        );

        return existing;
    }

    private void validateMinimumLetterCount(long totalLetterCount) {
        if (totalLetterCount < MIN_LETTER_COUNT) {
            throw new GeneralMessageException(
                    ReportErrorCode.REPORT_ANALYSIS_NOT_ENOUGH_LETTERS,
                    "분석하려면 편지 %d통이 필요해요.".formatted(MIN_LETTER_COUNT)
            );
        }
    }

    private void validateReanalyze(
            Long userId,
            ReportAnalysis analysis
    ) {
        LocalDateTime lastAnalyzedAt = analysis.getAnalyzedAt();

        long newLetterCount =
                countNewLettersSince(userId, lastAnalyzedAt);

        if (newLetterCount < MIN_NEW_LETTER_COUNT) {
            throw new GeneralMessageException(
                    ReportErrorCode.REPORT_ANALYSIS_NOT_ENOUGH_NEW_LETTERS,
                    "새 편지 %d통이 필요해요.".formatted(remainingNewLetterCount(newLetterCount))
            );
        }

        if (isAnalyzedThisWeek(lastAnalyzedAt)) {
            throw new GeneralException(
                    ReportErrorCode.REPORT_ANALYSIS_WEEKLY_LIMIT
            );
        }
    }

    private boolean isAnalyzedThisWeek(
            LocalDateTime analyzedAt
    ) {
        LocalDateTime thisMonday =
                LocalDate.now()
                        .with(DayOfWeek.MONDAY)
                        .atStartOfDay();

        return !analyzedAt.isBefore(thisMonday);
    }

    private Reanalyze evaluateReanalyze(
            Long userId,
            ReportAnalysis analysis
    ) {
        LocalDateTime lastAnalyzedAt =
                analysis.getAnalyzedAt();

        long newLetterCount =
                countNewLettersSince(
                        userId,
                        lastAnalyzedAt
                );

        boolean hasEnoughNewLetters =
                newLetterCount >= MIN_NEW_LETTER_COUNT;

        if (!hasEnoughNewLetters) {
            return Reanalyze.reanalyzedDisabled(
                    ReanalyzeReason.NOT_ENOUGH_NEW_LETTERS,
                    "새 편지 %d통이 필요해요.".formatted(remainingNewLetterCount(newLetterCount))
            );
        }

        if (isAnalyzedThisWeek(lastAnalyzedAt)) {
            return Reanalyze.reanalyzedDisabled(
                    ReanalyzeReason.WEEKLY_LIMIT,
                    "매주 월요일 분석이 초기화돼요."
            );
        }

        return Reanalyze.reanalyzedEnabled();
    }

    private long remainingNewLetterCount(long newLetterCount) {
        return MIN_NEW_LETTER_COUNT - newLetterCount;
    }

    private List<FromRanking> getTopFromRanking(Long userId) {
        List<FromLetterRankingProjection> topFroms =
                letterRepository.findTopFromsByLetterCount(userId);

        List<FromRanking> result = new ArrayList<>();

        for (int i = 0; i < topFroms.size(); i++) {
            FromLetterRankingProjection from = topFroms.get(i);

            result.add(
                    FromRanking.of(
                            i + 1,
                            from.getName(),
                            from.getLetterCount().intValue(),
                            from.getBgColor(),
                            from.getFontColor()
                    )
            );
        }

        return result;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new GeneralException(
                                ReportErrorCode.REPORT_NOT_FOUND_USER
                        )
                );
    }

    private User getUserForUpdate(Long userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new GeneralException(
                                ReportErrorCode.REPORT_NOT_FOUND_USER
                        )
                );
    }

    private long countVisibleLetters(Long userId) {
        return letterRepository.countVisibleLettersByUser(
                userId,
                LocalDateTime.now()
        );
    }

    private long countNewLettersSince(
            Long userId,
            LocalDateTime since
    ) {
        return letterRepository.countVisibleLettersByUserSince(
                userId,
                since
        );
    }

    private Optional<ReportAnalysis> findAnalysis(Long userId) {
        return reportAnalysisRepository.findByUserId(userId);
    }

    private ReportResponseDTO build(
            long totalLetterCount,
            List<FromRanking> fromRanking,
            Analysis analysis,
            Reanalyze reanalyze
    ) {
        return ReportResponseDTO.of(
                totalLetterCount,
                fromRanking,
                analysis,
                reanalyze
        );
    }
}