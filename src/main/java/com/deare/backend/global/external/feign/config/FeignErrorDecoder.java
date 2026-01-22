package com.deare.backend.global.external.feign.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();
    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String httpMethod = response.request().httpMethod().name();
        String url=response.request().url();
        String reason=response.reason();

        byte[] responseBody=extractResponseBody(response);

        if (status >= 500) {
            log.error(
                    "[Feign Error] methodKey={}, httpMethod={}, url={}, status={}, reason={}, responseBody={}",
                    methodKey,
                    httpMethod,
                    url,
                    status,
                    reason,
                    formatBody(responseBody)
            );
        } else {
            log.warn(
                    "[Feign Error] methodKey={}, httpMethod={}, url={}, status={}, reason={}, responseBody={}",
                    methodKey,
                    httpMethod,
                    url,
                    status,
                    reason,
                    formatBody(responseBody)
            );
        }

        //추후에 ExternalApiException으로 감쌀 예정
        Response newResponse=rebuildResponse(response, responseBody);
        return defaultErrorDecoder.decode(methodKey, newResponse);

    }

    private byte[] extractResponseBody(Response response) {
        if (response.body() == null) {
            return new byte[0];
        }

        try {
            return response.body().asInputStream().readAllBytes();

        } catch (Exception e) {
            return new byte[0];
        }
    }

    private String formatBody(byte[] bodyData){
        if(bodyData.length==0){
            return "EMPTY";
        }

        String body=new String(bodyData);
        return body.length() > 500 ? body.substring(0, 500) + "...(truncated)" : body;
    }

    private Response rebuildResponse(Response response, byte[] bodyData){
        return response.toBuilder()
                .body(bodyData)
                .build();
    }
}
