package com.deare.backend.api.setting.dto.response;

import com.deare.backend.domain.setting.entity.enums.MembershipPlan;

public record MembershipResponseDTO(
        MembershipPlan membershipPlan,
        boolean isPlus
) {}
