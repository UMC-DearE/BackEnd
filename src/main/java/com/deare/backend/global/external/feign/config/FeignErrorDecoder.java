package com.deare.backend.global.external.feign.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();
    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String httpMethod = response.request().httpMethod().name();
        String url=response.request().url();
        String reason=response.reason();

        String responseBody=extractResponseBody(response);

        if (status >= 500) {
            log.error(
                    "[Feign Error] methodKey={}, httpMethod={}, url={}, status={}, reason={}, responseBody={}",
                    methodKey,
                    httpMethod,
                    url,
                    status,
                    reason,
                    responseBody
            );
        } else {
            log.warn(
                    "[Feign Error] methodKey={}, httpMethod={}, url={}, status={}, reason={}, responseBody={}",
                    methodKey,
                    httpMethod,
                    url,
                    status,
                    reason,
                    responseBody
            );
        }

        //추후에 ExternalApiException으로 감쌀 예정
        return defaultErrorDecoder.decode(methodKey, response);

    }

    private String extractResponseBody(Response response) {
        if (response.body() == null) {
            return "EMPTY";
        }

        try {
            String body = new String(response.body().asInputStream().readAllBytes());

            return body.length() > 500 ? body.substring(0, 500) + "...(truncated)" : body;
        } catch (Exception e) {
            return "FAILED_TO_READ_BODY";
        }
    }
}
