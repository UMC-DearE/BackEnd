package com.deare.backend.api.auth;

import com.deare.backend.api.auth.dto.request.SignupRequestDTO;
import com.deare.backend.api.auth.dto.result.SignupResult;
import com.deare.backend.api.auth.dto.result.TokenPair;
import com.deare.backend.api.auth.exception.AuthErrorCode;
import com.deare.backend.api.auth.service.AuthService;
import com.deare.backend.domain.term.entity.Term;
import com.deare.backend.domain.term.entity.enums.TermType;
import com.deare.backend.domain.term.repository.TermRepository;
import com.deare.backend.domain.term.repository.UserTermRepository;
import com.deare.backend.domain.user.entity.enums.Provider;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.auth.jwt.JwtProvider;
import com.deare.backend.global.auth.jwt.JwtService;
import com.deare.backend.global.auth.signupToken.SignupTokenProvider;
import com.deare.backend.global.auth.signupToken.SignupTokenService;
import com.deare.backend.global.common.exception.GeneralException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class AuthServiceTest {

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;
    @Autowired private TermRepository termRepository;
    @Autowired private UserTermRepository userTermRepository;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private JwtService jwtService;
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
    @DisplayName("Signup Token 검증 성공")
    void validateSignupToken_success() {
        String token = signupTokenProvider.generateSignupToken("kakao", "valid123", "test@test.com");
        signupTokenService.saveSignupToken("kakao", "valid123", "test@test.com", token);

        assertThatCode(() -> authService.validateSignupToken(token)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Signup Token - Redis에 없으면 에러")
    void validateSignupToken_notInRedis() {
        String token = signupTokenProvider.generateSignupToken("kakao", "notexist", "test@test.com");

        assertThatThrownBy(() -> authService.validateSignupToken(token))
                .isInstanceOf(GeneralException.class)
                .satisfies(e -> assertThat(((GeneralException) e).getErrorCode()).isEqualTo(AuthErrorCode.EXPIRED_SIGNUP_TOKEN));
    }

    @Test
    @DisplayName("Signup Token - 잘못된 JWT 에러")
    void validateSignupToken_invalidJwt() {
        assertThatThrownBy(() -> authService.validateSignupToken("invalid.jwt.token"))
                .isInstanceOf(GeneralException.class)
                .satisfies(e -> assertThat(((GeneralException) e).getErrorCode()).isEqualTo(AuthErrorCode.INVALID_SIGNUP_TOKEN));
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        Term term = termRepository.save(new Term("이용약관", TermType.SERVICE, "내용", true));
        String token = signupTokenProvider.generateSignupToken("kakao", "newuser1", "new@test.com");
        signupTokenService.saveSignupToken("kakao", "newuser1", "new@test.com", token);

        SignupResult result = authService.signup(token, new SignupRequestDTO("유저", List.of(term.getId())));

        assertThat(result.tokenPair().accessToken()).isNotBlank();
        assertThat(result.tokenPair().refreshToken()).isNotBlank();
        assertThat(userRepository.findByProviderAndProviderId(Provider.KAKAO, "newuser1")).isPresent();
    }

    @Test
    @DisplayName("회원가입 - 1회성 보장")
    void signup_oneTimeUse() {
        Term term = termRepository.save(new Term("이용약관", TermType.SERVICE, "내용", true));
        String token = signupTokenProvider.generateSignupToken("kakao", "onetime1", "one@test.com");
        signupTokenService.saveSignupToken("kakao", "onetime1", "one@test.com", token);
        SignupRequestDTO request = new SignupRequestDTO("유저", List.of(term.getId()));

        authService.signup(token, request);

        assertThatThrownBy(() -> authService.signup(token, request))
                .isInstanceOf(GeneralException.class)
                .satisfies(e -> assertThat(((GeneralException) e).getErrorCode()).isEqualTo(AuthErrorCode.EXPIRED_SIGNUP_TOKEN));
    }

    @Test
    @DisplayName("회원가입 - 중복 가입 에러")
    void signup_alreadyExists() {
        userRepository.save(User.signUpUser(Provider.KAKAO, "existing1", "dup@test.com", "기존유저"));
        Term term = termRepository.save(new Term("이용약관", TermType.SERVICE, "내용", true));
        String token = signupTokenProvider.generateSignupToken("kakao", "existing1", "dup@test.com");
        signupTokenService.saveSignupToken("kakao", "existing1", "dup@test.com", token);

        assertThatThrownBy(() -> authService.signup(token, new SignupRequestDTO("중복", List.of(term.getId()))))
                .isInstanceOf(GeneralException.class)
                .satisfies(e -> assertThat(((GeneralException) e).getErrorCode()).isEqualTo(AuthErrorCode.USER_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("Refresh - 토큰 없으면 에러")
    void refresh_missingToken() {
        assertThatThrownBy(() -> authService.refresh(null))
                .isInstanceOf(GeneralException.class)
                .satisfies(e -> assertThat(((GeneralException) e).getErrorCode()).isEqualTo(AuthErrorCode.MISSING_REFRESH_TOKEN));
    }

    @Test
    @DisplayName("Refresh - 유효하지 않은 토큰 에러")
    void refresh_invalidToken() {
        assertThatThrownBy(() -> authService.refresh("invalid.token.here"))
                .isInstanceOf(GeneralException.class)
                .satisfies(e -> assertThat(((GeneralException) e).getErrorCode()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN));
    }

    @Test
    @DisplayName("Refresh 성공")
    void refresh_success() {
        User user = userRepository.save(User.signUpUser(Provider.KAKAO, "refresh1", "ref@test.com", "유저"));
        String rt = jwtProvider.generateRefreshToken(user);
        jwtService.saveRefreshToken(user.getId(), rt);

        TokenPair result = authService.refresh(rt);

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("Refresh - Redis 불일치 에러")
    void refresh_mismatch() throws InterruptedException {
        User user = userRepository.save(User.signUpUser(Provider.KAKAO, "mismatch1", "mis@test.com", "유저"));

        // 첫 번째 RT 생성 및 저장
        String rt1 = jwtProvider.generateRefreshToken(user);
        jwtService.saveRefreshToken(user.getId(), rt1);

        // 1초 대기 (JWT iat가 초 단위이므로 다른 토큰 생성 보장)
        Thread.sleep(1100);

        // 새로운 RT로 갱신 (기존 rt1은 Redis에서 덮어씌워짐)
        String rt2 = jwtProvider.generateRefreshToken(user);
        jwtService.saveRefreshToken(user.getId(), rt2);

        // 이전 토큰(rt1)으로 refresh 시도 → 불일치 에러
        assertThatThrownBy(() -> authService.refresh(rt1))
                .isInstanceOf(GeneralException.class)
                .satisfies(e -> assertThat(((GeneralException) e).getErrorCode()).isEqualTo(AuthErrorCode.REFRESH_TOKEN_MISMATCH));
    }

    @Test
    @DisplayName("약관 조회 성공")
    void getTerms_success() {
        termRepository.save(new Term("약관1", TermType.SERVICE, "내용", true));
        termRepository.save(new Term("약관2", TermType.PRIVACY, "내용", true));

        assertThat(authService.getSignupTerms().terms()).hasSize(2);
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() {
        User user = userRepository.save(User.signUpUser(Provider.KAKAO, "logout1", "out@test.com", "유저"));
        String rt = jwtProvider.generateRefreshToken(user);
        jwtService.saveRefreshToken(user.getId(), rt);

        authService.logout(user.getId());

        assertThat(jwtService.validateRefreshToken(user.getId(), rt)).isFalse();
    }
}
