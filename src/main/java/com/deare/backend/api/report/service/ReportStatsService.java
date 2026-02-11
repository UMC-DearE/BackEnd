package com.deare.backend.api.report.service;

import com.deare.backend.api.report.dto.response.EmotionDistributionDTO;
import com.deare.backend.api.report.dto.response.ReportStatsResponseDTO;
import com.deare.backend.api.report.dto.response.Top3FromDTO;
import com.deare.backend.domain.report.exception.ReportErrorCode;
import com.deare.backend.domain.user.entity.enums.Status;
import com.deare.backend.domain.user.entity.User;
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
                        new GeneralException(ReportErrorCode.REPORT_NOT_FOUND_USER)
                );

        if(user.getStatus().equals(Status.INACTIVE)){
            throw new GeneralException(ReportErrorCode.REPORT_FORBIDDEN);
        }

        return ReportStatsResponseDTO.builder()
                .isDummy(true)
                .top3From(List.of(
                        Top3FromDTO.of(1, "민서", 5),
                        Top3FromDTO.of(2, "엄마", 3),
                        Top3FromDTO.of(3, "디어리", 1)
                ))
                .topPhrases(List.of("보고싶어", "힘내","고마워","사랑해"))
                .emotionDistribution(List.of(
                        EmotionDistributionDTO.of("고마움",40),
                        EmotionDistributionDTO.of("즐거움",20),
                        EmotionDistributionDTO.of("위로",20),
                        EmotionDistributionDTO.of("그리움",5),
                        EmotionDistributionDTO.of("고민",10)
                ))
                .build();
    }
}
