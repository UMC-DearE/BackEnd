package com.deare.backend.api.auth;

import com.deare.backend.api.auth.dto.request.SignupRequestDTO;
import com.deare.backend.domain.term.entity.Term;
import com.deare.backend.domain.term.entity.TermType;
import com.deare.backend.domain.term.repository.TermRepository;
import com.deare.backend.domain.term.repository.UserTermRepository;
import com.deare.backend.domain.user.entity.Provider;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.auth.signupToken.SignupTokenProvider;
import com.deare.backend.global.auth.signupToken.SignupTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private TermRepository termRepository;
    @Autowired private UserTermRepository userTermRepository;
    @Autowired private SignupTokenProvider signupTokenProvider;
    @Autowired private SignupTokenService signupTokenService;
    @Autowired private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        userTermRepository.deleteAll();
        userRepository.deleteAll();
        termRepository.deleteAll();
        clearRedis();
    }

    private void clearRedis() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("OAuth Gate - Kakao URL 생성")
    void authorize_kakao() throws Exception {
        mockMvc.perform(get("/auth/oauth2/kakao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.authorizeUrl").value(containsString("kauth.kakao.com")));
    }

    @Test
    @DisplayName("OAuth Gate - Google URL 생성")
    void authorize_google() throws Exception {
        mockMvc.perform(get("/auth/oauth2/google"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.authorizeUrl").value(containsString("accounts.google.com")));
    }

    @Test
    @DisplayName("OAuth Gate - 잘못된 Provider")
    void authorize_invalidProvider() throws Exception {
        mockMvc.perform(get("/auth/oauth2/naver"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_400_1"));
    }

    @Test
    @DisplayName("약관 조회 - 토큰 없음")
    void getTerms_noToken() throws Exception {
        mockMvc.perform(get("/auth/terms"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_400_3"));
    }

    @Test
    @DisplayName("약관 조회 - 성공")
    void getTerms_success() throws Exception {
        termRepository.save(new Term("약관", TermType.SERVICE, "내용", true));
        String token = signupTokenProvider.generateSignupToken("kakao", "terms1", "t@test.com");
        signupTokenService.saveSignupToken("kakao", "terms1", "t@test.com", token);

        mockMvc.perform(get("/auth/terms").cookie(new Cookie("signup_token", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.terms").isArray());
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void signup_success() throws Exception {
        Term term = termRepository.save(new Term("약관", TermType.SERVICE, "내용", true));
        String token = signupTokenProvider.generateSignupToken("kakao", "signup1", "s@test.com");
        signupTokenService.saveSignupToken("kakao", "signup1", "s@test.com", token);

        mockMvc.perform(post("/auth/signup")
                        .cookie(new Cookie("signup_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupRequestDTO("유저", List.of(term.getId())))))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"));

        assertThat(userRepository.findByProviderAndProviderId(Provider.KAKAO, "signup1")).isPresent();
    }

    @Test
    @DisplayName("회원가입 - 토큰 없음")
    void signup_noToken() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupRequestDTO("유저", List.of(1L)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_400_3"));
    }

    @Test
    @DisplayName("JWT 재발급 - 토큰 없음")
    void refresh_noToken() throws Exception {
        mockMvc.perform(post("/auth/jwt/refresh"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_400_4"));
    }

    @Test
    @DisplayName("OAuth Callback - 잘못된 State")
    void callback_invalidState() throws Exception {
        mockMvc.perform(get("/auth/oauth2/kakao/callback")
                        .param("code", "code")
                        .param("state", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_400_2"));
    }
}
