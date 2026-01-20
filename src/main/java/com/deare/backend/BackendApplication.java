package com.deare.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        // ▼▼▼ 이 줄을 추가해서 실행 창(Console) 로그를 확인해 주세요 ▼▼▼
        System.out.println(">>> CHECK DB_URL: " + System.getenv("DB_URL"));

        SpringApplication.run(BackendApplication.class, args);
    }

}