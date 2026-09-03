package com.deare.backend.global.common.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ExceptionTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsBadRequestWhenRequiredParameterIsMissing() throws Exception {
        mockMvc.perform(get("/exception-test/required-param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(CommonErrorCode.REQUEST_BINDING_FAILED.getCode()));
    }

    @Test
    void returnsMethodNotAllowedForUnsupportedMethod() throws Exception {
        mockMvc.perform(post("/exception-test/required-param"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string(HttpHeaders.ALLOW, HttpMethod.GET.name()))
                .andExpect(jsonPath("$.code").value(CommonErrorCode.METHOD_NOT_ALLOWED.getCode()));
    }

    @Test
    void returnsUnsupportedMediaTypeForUnsupportedContentType() throws Exception {
        mockMvc.perform(post("/exception-test/body").contentType(MediaType.TEXT_PLAIN).content("body"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.MEDIA_TYPE_NOT_SUPPORTED.getCode()));
    }

    @Test
    void returnsValidationCodeForInvalidBody() throws Exception {
        mockMvc.perform(post("/exception-test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.VALIDATION_FAILED.getCode()));
    }

    @Test
    void preservesGeneralExceptionStatusAndCode() throws Exception {
        mockMvc.perform(get("/exception-test/general"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TEST_40901"))
                .andExpect(jsonPath("$.message").value("테스트 충돌입니다."));
    }

    @Test
    void returnsGenericInternalServerErrorForUnhandledException() throws Exception {
        mockMvc.perform(get("/exception-test/unhandled"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }

    @RestController
    @RequestMapping("/exception-test")
    private static class ExceptionTestController {

        @GetMapping("/required-param")
        String requiredParam(@RequestParam String value) {
            return value;
        }

        @PostMapping(value = "/body", consumes = MediaType.APPLICATION_JSON_VALUE)
        String body(@Valid @RequestBody TestRequest request) {
            return request.name();
        }

        @GetMapping("/general")
        String general() {
            throw new GeneralException(TestErrorCode.CONFLICT);
        }

        @GetMapping("/unhandled")
        String unhandled() {
            throw new IllegalStateException("internal detail");
        }
    }

    private record TestRequest(@NotBlank String name) {
    }

    private enum TestErrorCode implements BaseErrorCode {
        CONFLICT;

        public HttpStatus getStatus() { return HttpStatus.CONFLICT; }
        public String getCode() { return "TEST_40901"; }
        public String getMessage() { return "테스트 충돌입니다."; }
    }
}
