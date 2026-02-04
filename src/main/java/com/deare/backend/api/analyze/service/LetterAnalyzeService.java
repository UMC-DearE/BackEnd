package com.deare.backend.api.analyze.service;

import com.deare.backend.api.analyze.dto.request.AnalyzeLetterRequestDTO;
import com.deare.backend.api.analyze.dto.response.AnalyzeLetterResponseDTO;
import com.deare.backend.api.analyze.dto.response.ReAnalyzeResponseDTO;
import com.deare.backend.domain.emotion.entity.Emotion;
import com.deare.backend.domain.emotion.repository.EmotionRepository;
import com.deare.backend.global.external.gemini.adapter.analyze.AnalyzeAdapter;
import com.deare.backend.global.external.gemini.dto.response.analyze.AnalyzeResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LetterAnalyzeService {

    private final AnalyzeAdapter analyzeAdapter;
    private final EmotionRepository emotionRepository;

    public AnalyzeLetterResponseDTO analyze(AnalyzeLetterRequestDTO request){
        AnalyzeResponseDTO analyzeResult = analyzeAdapter.analyze(request.getContent());

        String summary=analyzeResult.getSummary();
        List<String> emotionList=analyzeResult.getEmotions();

        List<Emotion> emotions=emotionRepository.findByNameIn(emotionList);

        return AnalyzeLetterResponseDTO.of(summary,emotions);
    }

    public ReAnalyzeResponseDTO analyzeForUpdate(String content){
        AnalyzeResponseDTO analyzeResult = analyzeAdapter.analyze(content);

        String summary=analyzeResult.getSummary();
        List<String> emotionsName = analyzeResult.getEmotions();

        List<Emotion> emotions = emotionRepository.findByNameIn(emotionsName);

        return ReAnalyzeResponseDTO.of(summary,emotions);
    }
}
