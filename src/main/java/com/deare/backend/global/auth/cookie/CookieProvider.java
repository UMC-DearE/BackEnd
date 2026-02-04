package com.deare.backend.global.auth.cookie;

import com.deare.backend.global.auth.jwt.JwtProperties;
import com.deare.backend.global.auth.signupToken.SignupTokenProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CookieProvider {

    /** TODO
     * secure(true) 는 http 미지원 -> 일단 false 변경 후 nginx 도입 이후에 실제 서비스 배포에서는 true 바꾸기
     */

    private final JwtProperties jwtProperties;
    private final SignupTokenProperties signupTokenProperties;

    // === Refresh Token Cookie ===

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMillis(jwtProperties.getRefreshTokenExpiration()))
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie expireRefreshTokenCookie() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }

    // === Signup Token Cookie ===

    public ResponseCookie createSignupTokenCookie(String signupToken) {
        return ResponseCookie.from("signup_token", signupToken)
                .httpOnly(true)
                .secure(false)
                .path("/auth")
                .maxAge(Duration.ofMillis(signupTokenProperties.getExpiration()))
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie expireSignupTokenCookie() {
        return ResponseCookie.from("signup_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/auth")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }
}
