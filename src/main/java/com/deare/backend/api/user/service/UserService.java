package com.deare.backend.api.user.service;

import com.deare.backend.api.user.dto.ProfileResponseDTO;
import com.deare.backend.api.user.dto.ProfileUpdateRequestDTO;

public interface UserService {

    ProfileResponseDTO getMyProfile(Long userId);

    ProfileResponseDTO updateMyProfile(Long userId, ProfileUpdateRequestDTO request);
}
