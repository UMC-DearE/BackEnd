package com.deare.backend.api.user.dto.response;

import com.deare.backend.domain.setting.entity.enums.MembershipPlan;
import com.deare.backend.domain.user.entity.enums.Provider;

public record ProfileResponseDTO(
        Long userId,
        String nickname,
        String intro,
        String profileImageUrl,
        MembershipPlan membershipPlan,
        String email,
        Provider provider
) {
}
