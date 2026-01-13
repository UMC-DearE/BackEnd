package com.deare.backend.api.test.controller;

import com.deare.backend.api.test.exception.TestErrorCode;
import com.deare.backend.global.common.exception.GeneralException;
import com.deare.backend.global.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 성공 응답 테스트
     */
    @GetMapping("/success")
    public ApiResponse<String> success() {
        return ApiResponse.success("API Response 성공 테스트");
    }

    /**
     * ❌ GeneralException 테스트 (AUTH_40101)
     */
    @GetMapping("/unauthorized")
    public ApiResponse<Void> unauthorized() {
        throw new GeneralException(TestErrorCode.UNAUTHORIZED);
    }

    /**
     * ❌ 다른 에러 코드 테스트 (AUTH_40102)
     */
    @GetMapping("/invalid-password")
    public ApiResponse<Void> invalidPassword() {
        throw new GeneralException(TestErrorCode.INVALID_PASSWORD);
    }

    /**
     * ❌ 예상치 못한 예외 테스트 (500)
     */
    @GetMapping("/exception")
    public ApiResponse<Void> exception() {
        throw new RuntimeException("강제 예외 발생");
    }
}
