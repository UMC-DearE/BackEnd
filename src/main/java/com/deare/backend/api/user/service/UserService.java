package com.deare.backend.api.user.service;

import com.deare.backend.api.user.dto.response.ProfileResponseDTO;
import com.deare.backend.api.user.dto.request.ProfileUpdateRequestDTO;
import com.deare.backend.api.user.dto.response.ProfileUpdateResponseDTO;

public interface UserService {

    ProfileResponseDTO getMyProfile(Long userId);

    ProfileUpdateResponseDTO updateMyProfile(Long userId, ProfileUpdateRequestDTO request);
}
