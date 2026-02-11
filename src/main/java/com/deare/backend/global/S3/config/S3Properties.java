package com.deare.backend.global.S3.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(
        boolean enabled,
        String bucket,
        String region,
        String cloudfrontDomain
) {}
