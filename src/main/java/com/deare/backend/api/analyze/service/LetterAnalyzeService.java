package com.deare.backend.api.analyze.service;

import com.deare.backend.api.analyze.dto.request.AnalyzeLetterRequestDTO;
import com.deare.backend.api.analyze.dto.response.AnalyzeLetterResponseDTO;
import com.deare.backend.api.analyze.dto.response.ReAnalyzeResponseDTO;
import com.deare.backend.domain.emotion.entity.Emotion;
import com.deare.backend.domain.emotion.exception.EmotionErrorCode;
import com.deare.backend.domain.emotion.repository.EmotionRepository;
import com.deare.backend.global.common.exception.GeneralException;
import com.deare.backend.global.external.gemini.adapter.analyze.AnalyzeAdapter;
import com.deare.backend.global.external.gemini.dto.response.analyze.AnalyzeResponseDTO;
import com.deare.backend.global.external.gemini.limit.AiUsageLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class LetterAnalyzeService {

    private final AnalyzeAdapter analyzeAdapter;
    private final EmotionRepository emotionRepository;
    private final AiUsageLimiter aiUsageLimiter;

    public AnalyzeLetterResponseDTO analyze(AnalyzeLetterRequestDTO request, Long userId){
        AnalyzeResult result=getResult(request.getContent(), userId);
        return AnalyzeLetterResponseDTO.of(result.summary(), result.emotions());
    }

    public ReAnalyzeResponseDTO analyzeForUpdate(String content, Long userId){
        AnalyzeResult result = getResult(content, userId);
        return ReAnalyzeResponseDTO.of(result.summary(), result.emotions());
    }

    private AnalyzeResult getResult(String content, Long userId) {
        String usageKey = aiUsageLimiter.reserve(userId);

        // reserve() 이후 이 블록 안에서 무엇이 실패하든(AI 호출 실패, AI 응답 검증 실패 등)
        // 편지 분석이 최종적으로 성공하지 못한 것이므로, 전부 사용자 귀책이 아닌 실패로 보고 사용량을 반납한다.
        try {
            AnalyzeResponseDTO analyzeResult = analyzeAdapter.analyze(content);

        String summary=analyzeResult.getSummary();
        List<String> emotionsName = analyzeResult.getEmotions();

        validateEmotionCount(emotionsName);

        List<Emotion> emotions = emotionRepository.findByNameIn(emotionsName);

        validateEmotionExistence(emotionsName, emotions);
        return new AnalyzeResult(summary, emotions);
        } catch (RuntimeException e) {
            aiUsageLimiter.release(usageKey);
            throw e;
        }
    }

    private void validateEmotionCount(List<String> emotionNames){
        if(emotionNames==null || emotionNames.size()<2||emotionNames.size()>3){
            throw new GeneralException(EmotionErrorCode.INVALID_AI_RESPONSE);
        }
    }

    private void validateEmotionExistence(List<String> emotionNames, List<Emotion>emotions){
        if(emotions.size()!=emotionNames.size()){
            throw new GeneralException(EmotionErrorCode.EMOTION_NOT_EXIST);
        }
    }

    private record AnalyzeResult(String summary, List<Emotion> emotions) {
    }
}
