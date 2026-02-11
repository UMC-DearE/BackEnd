package com.deare.backend.api.home.dto.result;

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

