package com.example.hostdevtools.hostnumbering;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * "호스트 채번" 화면의 핵심 로직.
 *
 * 채번의 본질은 다음 번호를 순서대로 매기는 게 아니라, 운영 환경과 테스트 환경 양쪽에서
 * 이미 사용 중인 코드/포트를 모두 모아 어느 쪽에도 없는 조합을 찾는 것이다.
 *
 * - 테스트 DB: host-numbering.test-db.enabled=true(환경변수 TEST_DB_ENABLED)로 켜져
 *   있고 JdbcTemplate 빈이 존재하면 LINE_TCP_IP_INFO 테이블을 직접 조회한다. 꺼져
 *   있거나 조회 중 오류가 나면 seed()로 만든 Mock 데이터로 자동 대체된다 (앱이 테스트
 *   DB 없이도 항상 정상 동작하도록 하기 위한 안전장치). 실제 컬럼명은 아직 확인 전이라
 *   TestDbProperties의 기본값(HOST_CODE/PORT_NO)은 가정값이다 — peekColumns() 참고.
 * - 운영 데이터: 직접 조회가 불가능하다는 전제라, 사용자가 엑셀(가맹점명/코드/포트)을
 *   업로드하면 그 내용을 그대로 대체 저장한다.
 *
 * 포트는 "앞자리는 같고 끝자리만 다르다"(운영=1, 테스트=2)는 규칙만 확정되어 있고, 코드의
 * 숫자 접미사와 포트 앞자리 사이의 대응 규칙은 요구사항 정의서에 없어 별도 채번 풀로 가정했다
 * (확인 필요).
 */
@Service
public class HostNumberingService {

    private static final Logger log = LoggerFactory.getLogger(HostNumberingService.class);

    private static final Set<String> NON_GIRO_PREFIXES = Set.of("HD", "HJ", "HL", "HN");
    private static final Set<String> GIRO_PREFIXES = Set.of("HG");
    private static final int PORT_BASE_START = 3100;

    private final TestDbProperties testDbProperties;
    private final JdbcTemplate jdbcTemplate;

    private final List<TestEntry> mockTestEntries = new ArrayList<>();
    private final Instant mockSeededAt = Instant.now();

    private List<ProductionEntry> productionEntries = new ArrayList<>();
    private Instant productionLastUploadedAt;

    public HostNumberingService(TestDbProperties testDbProperties, ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
        this.testDbProperties = testDbProperties;
        this.jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        seedMockTestEntries();
    }

    private void seedMockTestEntries() {
        mockTestEntries.add(new TestEntry("HD01", 31012));
        mockTestEntries.add(new TestEntry("HD02", 31022));
        mockTestEntries.add(new TestEntry("HJ01", 31032));
        mockTestEntries.add(new TestEntry("HN01", 31042));
        mockTestEntries.add(new TestEntry("HG01", 31052));
    }

    /**
     * 테스트 DB가 켜져 있으면 실제 조회를, 아니면(또는 조회 실패 시) Mock 데이터를 반환한다.
     */
    private TestDbSnapshot loadTestEntries() {
        if (jdbcTemplate != null && testDbProperties.isEnabled()) {
            try {
                String sql = "SELECT " + testDbProperties.getCodeColumn() + ", " + testDbProperties.getPortColumn()
                        + " FROM " + testDbProperties.getTable();
                List<TestEntry> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
                    String code = rs.getString(1);
                    return new TestEntry(code == null ? "" : code.trim(), rs.getInt(2));
                });
                return new TestDbSnapshot(rows, true, Instant.now());
            } catch (DataAccessException e) {
                log.warn("테스트 DB({}) 조회 실패, Mock 데이터로 대체합니다: {}", testDbProperties.getTable(), e.getMessage());
            }
        }
        return new TestDbSnapshot(mockTestEntries, false, mockSeededAt);
    }

    public HostNumberingStatus status() {
        TestDbSnapshot snapshot = loadTestEntries();
        return new HostNumberingStatus(
                true,
                snapshot.real() ? "REAL" : "MOCK",
                snapshot.syncedAt().toString(),
                snapshot.entries().size(),
                productionLastUploadedAt != null,
                productionLastUploadedAt == null ? null : productionLastUploadedAt.toString(),
                productionEntries.size()
        );
    }

    /**
     * 확인 필요: 실제 LINE_TCP_IP_INFO 컬럼명을 몰라서 임시 컬럼명(HOST_CODE/PORT_NO)을
     * 가정해뒀다. 테스트 DB 연동을 켠 뒤 이 메서드로 실제 컬럼 목록을 확인하고,
     * TEST_DB_CODE_COLUMN / TEST_DB_PORT_COLUMN 환경변수를 실제 값으로 교체하면 된다.
     */
    public List<String> peekColumns() {
        if (jdbcTemplate == null) {
            throw new IllegalStateException(
                    "테스트 DB가 아직 설정되지 않았습니다. TEST_DB_ENABLED/TEST_DB_URL 등 환경변수를 확인하세요.");
        }
        String sql = "SELECT * FROM " + testDbProperties.getTable() + " WHERE ROWNUM <= 1";
        return jdbcTemplate.query(sql, rs -> {
            List<String> columns = new ArrayList<>();
            int count = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= count; i++) {
                columns.add(rs.getMetaData().getColumnName(i));
            }
            return columns;
        });
    }

    public synchronized HostNumberingStatus uploadProduction(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 엑셀 파일을 선택하세요.");
        }

        List<ProductionEntry> parsed = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue; // 헤더 행(가맹점명 / 코드 / 포트) 스킵
                }
                String merchantName = cellToString(row.getCell(0));
                String code = cellToString(row.getCell(1));
                String portText = cellToString(row.getCell(2));
                if (code == null || code.isBlank() || portText == null || portText.isBlank()) {
                    continue;
                }
                int port;
                try {
                    port = (int) Double.parseDouble(portText.trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                parsed.add(new ProductionEntry(merchantName == null ? "" : merchantName.trim(), code.trim(), port));
            }
        }

        productionEntries = parsed;
        productionLastUploadedAt = Instant.now();
        return status();
    }

    public byte[] buildProductionTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("운영 데이터");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("가맹점명");
            header.createCell(1).setCellValue("코드");
            header.createCell(2).setCellValue("포트");
            sheet.setColumnWidth(0, 6000);
            sheet.setColumnWidth(1, 3000);
            sheet.setColumnWidth(2, 3000);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private String cellToString(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BLANK -> null;
            default -> cell.toString();
        };
    }

    public synchronized RecommendResponse recommend(RecommendRequest request) {
        String prefix = request.getPrefix() == null ? "" : request.getPrefix().trim().toUpperCase();
        Set<String> validGroup = request.isInternetGiro() ? GIRO_PREFIXES : NON_GIRO_PREFIXES;
        if (!validGroup.contains(prefix)) {
            throw new IllegalArgumentException(
                    "인터넷 지로 채번 여부(" + (request.isInternetGiro() ? "예" : "아니오")
                            + ") 상태에서는 사용할 수 없는 코드 접두사입니다: " + prefix);
        }

        Set<String> usedCodes = new HashSet<>();
        Set<Integer> usedPortBases = new HashSet<>();
        for (TestEntry e : loadTestEntries().entries()) {
            usedCodes.add(e.code());
            usedPortBases.add(e.port() / 10);
        }
        for (ProductionEntry e : productionEntries) {
            usedCodes.add(e.code());
            usedPortBases.add(e.port() / 10);
        }

        String code = null;
        for (int n = 1; n <= 99; n++) {
            String candidate = prefix + String.format("%02d", n);
            if (!usedCodes.contains(candidate)) {
                code = candidate;
                break;
            }
        }
        if (code == null) {
            throw new IllegalStateException(prefix + " 접두사의 01~99 코드가 모두 사용 중입니다.");
        }

        int portBase = PORT_BASE_START;
        while (usedPortBases.contains(portBase)) {
            portBase++;
        }

        return new RecommendResponse(code, portBase * 10 + 1, portBase * 10 + 2);
    }

    private record TestDbSnapshot(List<TestEntry> entries, boolean real, Instant syncedAt) {}
}
