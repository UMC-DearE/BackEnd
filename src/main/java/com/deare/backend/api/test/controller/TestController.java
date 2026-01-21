package com.deare.backend.api.test.controller;

import com.deare.backend.api.test.exception.TestErrorCode;
import com.deare.backend.global.common.exception.GeneralException;
import com.deare.backend.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
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

    // (1) MethodArgumentTypeMismatchException: int에 문자열 넣기
    @GetMapping("/type-mismatch")
    public String typeMismatch(@RequestParam int page) {
        return "ok: " + page;
    }

    // (4) ConstraintViolationException: @Min 위반
    @GetMapping("/constraint")
    public String constraint(@RequestParam @Min(1) int page) {
        return "ok: " + page;
    }

    // (3) HttpMessageNotReadableException: JSON 파싱 실패는 body로 재현
    @PostMapping("/body")
    public String body(@RequestBody @Valid SampleBody body) {
        return "ok";
    }

    public record SampleBody(
            @NotNull Integer age,
            @NotBlank String name
    ) {}
}
