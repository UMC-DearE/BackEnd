package com.deare.backend.domain.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Provider {

    KAKAO("카카오"),
    GOOGLE("구글");

    private final String description;
}
