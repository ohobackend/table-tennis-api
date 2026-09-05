package com.tabletennis.app.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {

    // Railway가 주는 DATABASE_URL (예: postgresql://user:pass@host:5432/db)
    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    // 로컬 개발용 기존 fallback 값 (application.yml 기존 설정 그대로 사용)
    @Value("${spring.datasource.url}")
    private String fallbackUrl;
    @Value("${spring.datasource.username}")
    private String fallbackUsername;
    @Value("${spring.datasource.password}")
    private String fallbackPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();

        if (databaseUrl != null && !databaseUrl.isBlank()) {
            URI uri = URI.create(databaseUrl);
            String[] userInfo = uri.getUserInfo() != null ? uri.getUserInfo().split(":", 2) : new String[]{"", ""};
            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            String query = uri.getQuery() != null ? "?" + uri.getQuery() : "";
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath() + query;

            ds.setJdbcUrl(jdbcUrl);
            ds.setUsername(userInfo[0]);
            ds.setPassword(userInfo.length > 1 ? userInfo[1] : "");
        } else {
            // 로컬 개발 환경 (docker-compose 등)에서는 기존 방식 그대로
            ds.setJdbcUrl(fallbackUrl);
            ds.setUsername(fallbackUsername);
            ds.setPassword(fallbackPassword);
        }
        return ds;
    }
}
