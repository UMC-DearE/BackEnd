package com.deare.backend.global.auth.oauth.controller;

import com.deare.backend.global.auth.oauth.dto.oauth.OAuthAuthorizeResponseDTO;
import com.deare.backend.global.auth.oauth.dto.oauth.OAuthCallbackUserInfoResponseDTO;
import com.deare.backend.global.auth.oauth.service.OAuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/oauth2")
public class OAuthController {

    private final OAuthService oauthService;

    public OAuthController(OAuthService oauthService) {
        this.oauthService = oauthService;
    }

    // 1) code 발급 전: authorizeUrl 반환
    @GetMapping("/{provider}")
    public OAuthAuthorizeResponseDTO authorizeUrl(@PathVariable String provider) {
        return oauthService.buildAuthorizeUrl(provider);
    }

    // 2) callback: code -> token -> userinfo -> (provider, providerUserId, email)만 반환
    @GetMapping("/{provider}/callback")
    public OAuthCallbackUserInfoResponseDTO callback(
            @PathVariable String provider,
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state
    ) {
        // state 검증은 다음 단계(권장: Redis 저장/검증)
        return oauthService.handleCallback(provider, code);
    }
}
