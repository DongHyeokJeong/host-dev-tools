package com.example.hostdevtools.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 허용할 프론트엔드 Origin은 app.cors.allowed-origins(환경변수 CORS_ALLOWED_ORIGINS)로
 * 설정한다. 기본값은 로컬 개발용 Vite 포트(3000)뿐이라, 내부망 등 다른 호스트/포트에서
 * 프론트엔드를 서빙할 때는 배포 환경에 맞는 Origin으로 반드시 교체해야 한다
 * (README "환경변수" 절 참고).
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }
}
