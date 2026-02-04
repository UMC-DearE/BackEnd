package com.deare.backend.api.report.service;

import com.deare.backend.api.report.dto.response.EmotionDistributionDTO;
import com.deare.backend.api.report.dto.response.ReportStatsResponseDTO;
import com.deare.backend.api.report.dto.response.Top3FromDTO;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.exception.UserErrorCode;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportStatsService {

    private final UserRepository userRepository;

    public ReportStatsResponseDTO getDummyStatics(Long userId){

        User user=userRepository.findById(userId)
                .orElseThrow(()->
                        new GeneralException(UserErrorCode.USER_NOT_FOUND)
                );

        return ReportStatsResponseDTO.builder()
                .isDummy(true)
                .top3From(List.of(
                        Top3FromDTO.of(1, "민서", 5),
                        Top3FromDTO.of(2, "엄마", 3),
                        Top3FromDTO.of(3, "디어리", 1)
                ))
                .topPhrases(List.of("보고싶어", "힘내","고마워","사랑해"))
                .emotionDistribution(List.of(
                        EmotionDistributionDTO.of("고마움",40.0),
                        EmotionDistributionDTO.of("즐거움",20.0),
                        EmotionDistributionDTO.of("위로",20.0),
                        EmotionDistributionDTO.of("그리움",5.0),
                        EmotionDistributionDTO.of("고민",10.0)
                ))
                .build();
    }
}
