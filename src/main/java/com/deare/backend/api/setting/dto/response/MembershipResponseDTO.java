package com.deare.backend.api.setting.dto.response;

import com.deare.backend.domain.setting.entity.MembershipPlan;

public record MembershipResponseDTO(
        MembershipPlan membershipPlan,
        boolean isPlus
) {}
