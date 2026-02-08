package com.deare.backend.api.from.service;

import com.deare.backend.api.from.dto.*;
import com.deare.backend.domain.from.entity.From;
import com.deare.backend.domain.from.exception.FromErrorCode;
import com.deare.backend.domain.from.repository.FromRepository;
import com.deare.backend.domain.from.repository.query.FromQueryRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.exception.UserErrorCode;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FromService {

    private final FromRepository fromRepository;
    private final FromQueryRepository fromQueryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public FromListResponseDTO getFroms(Long userId) {
        return new FromListResponseDTO(
                fromQueryRepository.findFromsWithLetterCount(userId)
        );
    }

    @Transactional
    public FromCreateResponseDTO createFrom(Long userId, FromCreateRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_NOT_FOUND));

        From from = new From(
                request.name(),
                request.bgColor(),
                request.fontColor(),
                user
        );

        From saved = fromRepository.save(from);
        return new FromCreateResponseDTO(saved.getId());
    }

    @Transactional
    public FromUpdateResponseDTO updateFrom(Long userId, Long fromId, FromUpdateRequestDTO request) {

        From from = fromRepository.findById(fromId)
                .orElseThrow(() -> new GeneralException(FromErrorCode.FROM_40401));

        if (!from.isOwnedBy(userId)) {
            throw new GeneralException(FromErrorCode.FROM_40301);
        }

        if (request.name() != null) {
            from.changeFromName(request.name());
        }

        boolean bgProvided = request.bgColor() != null;
        boolean fontProvided = request.fontColor() != null;

        if (bgProvided ^ fontProvided) {
            throw new GeneralException(FromErrorCode.FROM_40001);
        }

        if (bgProvided && fontProvided) {
            from.changeColors(request.bgColor(), request.fontColor());
        }

        return new FromUpdateResponseDTO(
                from.getId(),
                from.getName(),
                from.getBackgroundColor(),
                from.getFontColor(),
                from.getUpdatedAt()
        );
    }

    @Transactional
    public FromDeleteDTO deleteFrom(Long userId, Long fromId) {

        From from = fromRepository.findById(fromId)
                .orElseThrow(() -> new GeneralException(FromErrorCode.FROM_40401));

        if (!from.isOwnedBy(userId)) {
            throw new GeneralException(FromErrorCode.FROM_40301);
        }

        fromRepository.delete(from);

        return new FromDeleteDTO(fromId);
    }
}
