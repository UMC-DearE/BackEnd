package com.deare.backend.api.user.dto.response;

import com.deare.backend.domain.setting.entity.enums.MembershipPlan;

public record ProfileResponseDTO(
        Long userId,
        String nickname,
        String intro,
        String profileImageUrl,
        MembershipPlan membershipPlan
) {
}
