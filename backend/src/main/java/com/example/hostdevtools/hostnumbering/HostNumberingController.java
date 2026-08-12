package com.example.hostdevtools.hostnumbering;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/host-numbering")
public class HostNumberingController {

    private final HostNumberingService service;

    public HostNumberingController(HostNumberingService service) {
        this.service = service;
    }

    @GetMapping("/status")
    public HostNumberingStatus status() {
        return service.status();
    }

    // 테스트 DB 연동을 켠 뒤 LINE_TCP_IP_INFO의 실제 컬럼명을 확인하기 위한 진단용 엔드포인트.
    // 확인되면 TEST_DB_CODE_COLUMN / TEST_DB_PORT_COLUMN 환경변수를 실제 값으로 교체한다.
    @GetMapping("/test-db/columns")
    public ResponseEntity<?> peekTestDbColumns() {
        try {
            return ResponseEntity.ok(Map.of("columns", service.peekColumns()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "테스트 DB 조회 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/production-template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        byte[] bytes = service.buildProductionTemplate();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"host-numbering-production-template.xlsx\"")
                .body(bytes);
    }

    @PostMapping(path = "/production-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProduction(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(service.uploadProduction(file));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "엑셀 파일을 읽지 못했습니다: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "엑셀 형식을 확인하세요: " + e.getMessage()));
        }
    }

    @PostMapping("/recommend")
    public ResponseEntity<?> recommend(@RequestBody RecommendRequest request) {
        try {
            return ResponseEntity.ok(service.recommend(request));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
