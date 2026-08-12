package com.example.hostdevtools.hostnumbering;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * "호스트 채번" 화면의 테스트 DB(Oracle) 접속 정보.
 *
 * application.properties의 host-numbering.test-db.* 값을 그대로 받는다. 실제 값은
 * 코드가 아니라 환경변수(TEST_DB_URL 등)로 주입하도록 되어 있다 (application.properties
 * 참고). enabled=false(기본값)이면 TestDbConfig가 DataSource/JdbcTemplate 빈을 아예
 * 만들지 않으므로, HostNumberingService는 지금까지처럼 Mock 데이터로 동작한다.
 *
 * 확인 필요: code-column/port-column 기본값(HOST_CODE/PORT_NO)은 실제 LINE_TCP_IP_INFO
 * 테이블 컬럼명을 몰라서 임시로 가정한 값이다. enabled=true로 켠 뒤
 * GET /api/host-numbering/test-db/columns 로 실제 컬럼명을 확인하고
 * TEST_DB_CODE_COLUMN / TEST_DB_PORT_COLUMN 환경변수로 교체해야 한다.
 */
@Component
@ConfigurationProperties(prefix = "host-numbering.test-db")
public class TestDbProperties {

    private boolean enabled = false;
    private String url;
    private String username;
    private String password;
    private String table = "LINE_TCP_IP_INFO";
    private String codeColumn = "HOST_CODE";
    private String portColumn = "PORT_NO";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getCodeColumn() {
        return codeColumn;
    }

    public void setCodeColumn(String codeColumn) {
        this.codeColumn = codeColumn;
    }

    public String getPortColumn() {
        return portColumn;
    }

    public void setPortColumn(String portColumn) {
        this.portColumn = portColumn;
    }
}
