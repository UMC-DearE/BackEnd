package com.deare.backend.api.from.service;

import com.deare.backend.api.from.dto.request.FromCreateRequestDTO;
import com.deare.backend.api.from.dto.request.FromUpdateRequestDTO;
import com.deare.backend.api.from.dto.response.FromCreateResponseDTO;
import com.deare.backend.api.from.dto.response.FromDeleteResponseDTO;
import com.deare.backend.api.from.dto.response.FromListResponseDTO;
import com.deare.backend.api.from.dto.response.FromUpdateResponseDTO;

public interface FromService {

    FromListResponseDTO getFroms(Long userId);

    FromCreateResponseDTO createFrom(Long userId, FromCreateRequestDTO request);

    FromUpdateResponseDTO updateFrom(Long userId, Long fromId, FromUpdateRequestDTO request);

    FromDeleteResponseDTO deleteFrom(Long userId, Long fromId);
}
