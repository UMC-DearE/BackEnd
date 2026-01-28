package com.deare.backend.domain.home.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HomeUserDto {
    private Long userId;
    private String nickname;
    private String intro;
    private String imgUrl;
}

