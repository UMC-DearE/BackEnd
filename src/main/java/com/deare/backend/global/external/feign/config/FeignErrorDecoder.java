package com.deare.backend.global.external.feign.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();
    @Override
    public Exception decode(String methodKey, Response response) {

        log.error("[Feign Error] methodKey={}, status={}",
                methodKey,
                response.status()
        );

        //추후에 ExternalApiException으로 감쌀 예정
        return defaultErrorDecoder.decode(methodKey, response);

    }
}
