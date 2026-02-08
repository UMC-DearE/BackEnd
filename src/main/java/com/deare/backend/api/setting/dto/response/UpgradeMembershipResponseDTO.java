package com.deare.backend.api.setting.dto.response;

import com.deare.backend.domain.setting.entity.MembershipPlan;

import java.time.LocalDateTime;

public record UpgradeMembershipResponseDTO(
        MembershipPlan membershipPlan,
        boolean isPlus,
        LocalDateTime updatedAt
) {}
