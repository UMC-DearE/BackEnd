package com.deare.backend.api.user.service;

import com.deare.backend.api.user.dto.response.ProfileResponseDTO;
import com.deare.backend.api.user.dto.request.ProfileUpdateRequestDTO;
import com.deare.backend.api.user.dto.response.ProfileUpdateResponseDTO;

public interface UserService {

    ProfileResponseDTO getMyProfile(Long userId);

    ProfileUpdateResponseDTO updateMyProfile(Long userId, ProfileUpdateRequestDTO request);

    /**
     * 회원 탈퇴 (소프트 딜리트)
     * - 30일 후 스케줄러에 의해 하드 딜리트
     */
    void deactivateUser(Long userId);
}
