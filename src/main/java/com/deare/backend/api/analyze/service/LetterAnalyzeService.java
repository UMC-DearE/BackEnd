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

    public AnalyzeLetterResponseDTO analyze(AnalyzeLetterRequestDTO request){
        AnalyzeResult result=getResult(request.getContent());
        return AnalyzeLetterResponseDTO.of(result.summary(), result.emotions());
    }

    public ReAnalyzeResponseDTO analyzeForUpdate(String content){
        AnalyzeResult result = getResult(content);
        return ReAnalyzeResponseDTO.of(result.summary(), result.emotions());
    }

    private AnalyzeResult getResult(String content) {
        AnalyzeResponseDTO analyzeResult = analyzeAdapter.analyze(content);

        String summary=analyzeResult.getSummary();
        List<String> emotionsName = analyzeResult.getEmotions();

        validateEmotionCount(emotionsName);

        List<Emotion> emotions = emotionRepository.findByNameIn(emotionsName);

        validateEmotionExistence(emotionsName, emotions);
        return new AnalyzeResult(summary, emotions);
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
