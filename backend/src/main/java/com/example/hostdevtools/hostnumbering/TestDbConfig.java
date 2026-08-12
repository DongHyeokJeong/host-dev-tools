package com.example.hostdevtools.hostnumbering;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariDataSource;

/**
 * host-numbering.test-db.enabled=true 일 때만 테스트 DB용 DataSource/JdbcTemplate 빈을
 * 만든다. false(기본값)면 이 클래스는 아무 빈도 만들지 않고, HostNumberingService는
 * JdbcTemplate이 없다는 걸 감지해서 Mock 데이터로 동작한다 (앱이 테스트 DB 없이도
 * 항상 뜰 수 있도록 하기 위한 의도적인 안전장치).
 */
@Configuration
public class TestDbConfig {

    @Bean
    @ConditionalOnProperty(prefix = "host-numbering.test-db", name = "enabled", havingValue = "true")
    public DataSource testDbDataSource(TestDbProperties props) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(props.getUrl());
        dataSource.setUsername(props.getUsername());
        dataSource.setPassword(props.getPassword());
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        dataSource.setMaximumPoolSize(3);
        dataSource.setPoolName("test-db-pool");
        return dataSource;
    }

    @Bean
    @ConditionalOnProperty(prefix = "host-numbering.test-db", name = "enabled", havingValue = "true")
    public JdbcTemplate testDbJdbcTemplate(DataSource testDbDataSource) {
        return new JdbcTemplate(testDbDataSource);
    }
}
