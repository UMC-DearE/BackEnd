package com.deare.backend.api.from.service;

import com.deare.backend.api.from.dto.request.FromCreateRequestDTO;
import com.deare.backend.api.from.dto.request.FromUpdateRequestDTO;
import com.deare.backend.api.from.dto.response.FromCreateResponseDTO;
import com.deare.backend.api.from.dto.response.FromDeleteResponseDTO;
import com.deare.backend.api.from.dto.response.FromListResponseDTO;
import com.deare.backend.api.from.dto.response.FromUpdateResponseDTO;
import com.deare.backend.domain.from.entity.From;
import com.deare.backend.domain.from.exception.FromErrorCode;
import com.deare.backend.domain.from.repository.FromRepository;
import com.deare.backend.domain.from.repository.query.FromQueryRepository;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.exception.UserErrorCode;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FromService {

    private final FromRepository fromRepository;
    private final FromQueryRepository fromQueryRepository;
    private final UserRepository userRepository;
    private final LetterRepository letterRepository;

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

        From from = fromRepository.findByIdAndIsDeletedFalse(fromId)
                .orElseThrow(() -> new GeneralException(FromErrorCode.FROM_40401));

        if (!from.isOwnedBy(userId)) {
            throw new GeneralException(FromErrorCode.FROM_40301);
        }

        boolean nameProvided = request.name() != null;
        boolean bgProvided = request.bgColor() != null;
        boolean fontProvided = request.fontColor() != null;

        if (!nameProvided && !bgProvided && !fontProvided) {
            throw new GeneralException(FromErrorCode.FROM_40002);
        }

        if (bgProvided ^ fontProvided) {
            throw new GeneralException(FromErrorCode.FROM_40001);
        }

        if (nameProvided) {
            from.changeFromName(request.name());
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
    public FromDeleteResponseDTO deleteFrom(Long userId, Long fromId) {

        From from = fromRepository.findByIdAndIsDeletedFalse(fromId)
                .orElseThrow(() -> new GeneralException(FromErrorCode.FROM_40401));

        if (!from.isOwnedBy(userId)) {
            throw new GeneralException(FromErrorCode.FROM_40301);
        }

        from.softDelete();

        int affected = letterRepository.softDeleteAllByUserIdAndFromId(userId, fromId);

        return new FromDeleteResponseDTO(fromId);
    }
}
