package com.example.hostdevtools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

// 테스트 DB 연결 정보가 없어도(host-numbering.test-db.enabled=false, 기본값) 앱이 항상
// 뜨도록 Spring Boot의 기본 DataSource 자동 설정은 꺼두고, hostnumbering.TestDbConfig에서
// 설정이 켜졌을 때만 수동으로 DataSource/JdbcTemplate 빈을 만든다.
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class HostDevToolsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HostDevToolsApplication.class, args);
    }
}
