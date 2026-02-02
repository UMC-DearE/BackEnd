package com.deare.backend.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
@Configuration
@Profile("test")
@EnableConfigurationProperties(S3Properties.class)
public class TestS3Config {

    @Bean
    @Primary
    public DefaultCredentialsProvider defaultCredentialsProvider() {
        return DefaultCredentialsProvider.create();
    }

    @Bean
    @Primary
    public S3Client s3Client(S3Properties props, DefaultCredentialsProvider cp) {
        return S3Client.builder()
                .region(Region.of(props.region()))
                .credentialsProvider(cp)
                .build();
    }

    @Bean
    @Primary
    public S3Presigner s3Presigner(S3Properties props, DefaultCredentialsProvider cp) {
        return S3Presigner.builder()
                .region(Region.of(props.region()))
                .credentialsProvider(cp)
                 .build();
    }
}