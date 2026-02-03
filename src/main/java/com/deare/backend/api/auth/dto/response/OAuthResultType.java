package com.deare.backend.api.auth.dto.response;

/**
 * OAuth 콜백 결과 타입
 */
public enum OAuthResultType {
    REGISTERED,       // 기존 가입자
    SIGNUP_REQUIRED   // 미가입자 (회원가입 필요)
}
