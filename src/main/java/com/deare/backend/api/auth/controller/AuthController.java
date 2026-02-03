package com.deare.backend.api.auth.controller;

import com.deare.backend.api.auth.dto.request.SignupRequestDTO;
import com.deare.backend.api.auth.dto.response.OAuthCallbackResponseDTO;
import com.deare.backend.api.auth.dto.response.SignupResponseDTO;
import com.deare.backend.api.auth.dto.response.TermResponseDTO;
import com.deare.backend.api.auth.dto.result.OAuthCallbackResult;
import com.deare.backend.api.auth.dto.result.SignupResult;
import com.deare.backend.api.auth.dto.result.TokenPair;
import com.deare.backend.api.auth.exception.AuthErrorCode;
import com.deare.backend.api.auth.service.AuthService;
import com.deare.backend.global.auth.cookie.CookieProvider;
import com.deare.backend.global.auth.oauth.dto.oauth.OAuthAuthorizeResponseDTO;
import com.deare.backend.global.auth.oauth.service.OAuthService;
import com.deare.backend.global.common.exception.GeneralException;
import com.deare.backend.global.common.response.ApiResponse;
import com.deare.backend.global.auth.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증/인가 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuthService oauthService;
    private final AuthService authService;
    private final CookieProvider cookieProvider;

    @Operation(
            summary = "OAuth 인증 URL 생성",
            description = "소셜 로그인을 위한 OAuth 인증 URL을 생성합니다. 클라이언트는 반환된 URL로 리다이렉트하여 소셜 로그인을 진행합니다."
    )
    @GetMapping("/oauth2/{provider}")
    public ResponseEntity<ApiResponse<OAuthAuthorizeResponseDTO>> authorize(
            @Parameter(description = "OAuth 제공자 (kakao, google)", example = "kakao")
            @PathVariable String provider
    ) {
        OAuthAuthorizeResponseDTO data = oauthService.buildAuthorizeUrl(provider);
        return ResponseEntity.ok(ApiResponse.success("OAuth인증 코드 발급에 성공하였습니다.", data));
    }

    @Operation(
            summary = "OAuth 콜백 처리",
            description = "소셜 로그인 후 콜백을 처리합니다. 기존 회원은 JWT를 발급하고, 신규 회원은 회원가입용 Signup Token을 발급합니다."
    )
    @GetMapping("/oauth2/{provider}/callback")
    public ResponseEntity<ApiResponse<OAuthCallbackResponseDTO>> callback(
            @Parameter(description = "OAuth 제공자 (kakao, google)", example = "kakao")
            @PathVariable String provider,
            @Parameter(description = "OAuth 인가 코드")
            @RequestParam("code") String code,
            @Parameter(description = "CSRF 방지용 state 값")
            @RequestParam("state") String state
    ) {
        // State 검증 (CSRF 방지)
        oauthService.validateState(state);

        OAuthCallbackResult result = authService.handleOAuthCallback(provider, code);

        if (result.isRegistered()) {
            // 기존 회원 -> JWT 발급 (at/rt)
            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + result.accessToken())
                    .header(HttpHeaders.SET_COOKIE, cookieProvider.createRefreshTokenCookie(result.refreshToken()).toString())
                    .body(ApiResponse.success("소셜 로그인 인증에 성공하였습니다.", result.response()));
        }

        // 신규 회원 -> Signup Token : HttpOnly Cookie
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieProvider.createSignupTokenCookie(result.signupToken()).toString())
                .body(ApiResponse.success("소셜 로그인 인증에 성공하였습니다.", result.response()));
    }

    @Operation(
            summary = "JWT 토큰 재발급",
            description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급합니다. Refresh Token은 HttpOnly Cookie로 전달됩니다."
    )
    @PostMapping("/jwt/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(
            @Parameter(hidden = true)
            @CookieValue(name = "refresh_token", required = false) String refreshToken
    ) {
        TokenPair tokenPair = authService.refresh(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPair.accessToken())
                .header(HttpHeaders.SET_COOKIE, cookieProvider.createRefreshTokenCookie(tokenPair.refreshToken()).toString())
                .body(ApiResponse.success("토큰 발행에 성공하였습니다.", null));
    }

    @Operation(
            summary = "회원가입용 약관 조회",
            description = "회원가입에 필요한 약관 목록을 조회합니다. Signup Token이 Cookie에 포함되어 있어야 합니다."
    )
    @GetMapping("/terms")
    public ResponseEntity<ApiResponse<TermResponseDTO>> getTerms(
            @Parameter(hidden = true)
            @CookieValue(name = "signup_token", required = false) String signupToken
    ) {
        // Signup Token 검증 Redis <-> 브라우저
        if (signupToken == null || signupToken.isBlank()) {
            throw new GeneralException(AuthErrorCode.MISSING_SIGNUP_TOKEN);
        }
        authService.validateSignupToken(signupToken);

        TermResponseDTO data = authService.getSignupTerms();

        return ResponseEntity.ok(ApiResponse.success("회원 가입용 약관 조회에 성공하였습니다.", data));
    }

    @Operation(
            summary = "회원가입",
            description = "신규 회원가입을 처리합니다. Signup Token(Cookie)과 약관 동의 정보가 필요합니다. 가입 완료 후 JWT를 발급합니다."
    )
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponseDTO>> signup(
            @Parameter(hidden = true)
            @CookieValue(name = "signup_token", required = false) String signupToken,
            @Valid @RequestBody SignupRequestDTO request
    ) {
        if (signupToken == null || signupToken.isBlank()) {
            throw new GeneralException(AuthErrorCode.MISSING_SIGNUP_TOKEN);
        }

        // 회원가입 처리 (내부에서 Redis 검증 + 삭제)
        SignupResult result = authService.signup(signupToken, request);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + result.tokenPair().accessToken())
                .header(HttpHeaders.SET_COOKIE, cookieProvider.createRefreshTokenCookie(result.tokenPair().refreshToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieProvider.expireSignupTokenCookie().toString())
                .body(ApiResponse.success("회원가입에 성공하였습니다.", result.response()));
    }

    @Operation(
            summary = "로그아웃",
            description = "로그아웃을 처리합니다. Redis에서 Refresh Token을 삭제하고 Cookie를 만료시킵니다."
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        Long userId = SecurityUtil.getCurrentUserId();
        authService.logout(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieProvider.expireRefreshTokenCookie().toString())
                .body(ApiResponse.success("로그아웃이 완료되었습니다.", null));
    }
}
