package com.deare.backend.global.auth.signupToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SignupTokenProvider {
    
    private final SignupTokenProperties signupTokenProperties;
    
    /**
     * Signup Token 생성
     * provider + providerId + email을 토큰에 담음
     */
    public String generateSignupToken(String provider, String providerId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + signupTokenProperties.getExpiration());
        
        return Jwts.builder()
                .claim("type", "SIGNUP")
                .claim("provider", provider)
                .claim("providerId", providerId)
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Signup Token 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Signup Token에서 정보 추출
     */
    public Map<String, String> parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        Map<String, String> result = new HashMap<>();
        result.put("provider", claims.get("provider", String.class));
        result.put("providerId", claims.get("providerId", String.class));
        result.put("email", claims.get("email", String.class));
        
        return result;
    }
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = signupTokenProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
