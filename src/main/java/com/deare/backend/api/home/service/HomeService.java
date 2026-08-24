package com.deare.backend.api.home.service;

import com.deare.backend.api.home.dto.request.HomeEditRequestDTO;
import com.deare.backend.api.home.dto.response.HomeDashboardResponse;

public interface HomeService {

    HomeDashboardResponse getHome(Long userId);

    void editHome(Long userId, HomeEditRequestDTO request);
}
