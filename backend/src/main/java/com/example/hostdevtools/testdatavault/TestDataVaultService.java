package com.example.hostdevtools.testdatavault;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

/**
 * "테스트 데이터함" 화면의 프로세스/전문 저장·조회·삭제 로직.
 *
 * DB 대신 backend 실행 위치 기준 data/test-data-vault.json 파일에 전체 상태(등록된
 * 프로세스 목록 + 저장된 전문 목록)를 그대로 읽고 쓴다. 여러 인스턴스가 동시에 떠서
 * 같은 파일에 쓰는 상황은 고려하지 않은 단일 서버용 골격이다 (확인 필요).
 *
 * 왼쪽 사이드바에 뜨는 "프로세스"는 "ongwhtion44", "ongwhtioj13"처럼 영숫자 코드
 * 체계이고, 승인거래/취소거래는 프로세스가 아니라 각 전문의 "거래 구분" 값이다.
 */
@Service
public class TestDataVaultService {

    private static final Path STORE_FILE = Paths.get("data", "test-data-vault.json");
    private static final List<String> DEFAULT_PROCESSES = List.of("ongwhtion44", "ongwhtioj13");

    private final ObjectMapper mapper = new ObjectMapper();
    private List<String> processes = new ArrayList<>();
    private List<TestDataEntry> entries = new ArrayList<>();

    public TestDataVaultService() {
        load();
    }

    private synchronized void load() {
        if (!Files.exists(STORE_FILE)) {
            processes = new ArrayList<>(DEFAULT_PROCESSES);
            entries = new ArrayList<>(seedExampleEntries());
            persist();
            return;
        }
        try {
            VaultStore store = mapper.readValue(STORE_FILE.toFile(), VaultStore.class);
            processes = new ArrayList<>(store.processes() != null ? store.processes() : List.of());
            entries = new ArrayList<>(store.entries() != null ? store.entries() : List.of());
        } catch (IOException e) {
            throw new UncheckedIOException("테스트 데이터함 파일을 읽지 못했습니다: " + STORE_FILE, e);
        }
    }

    private List<TestDataEntry> seedExampleEntries() {
        Instant now = Instant.now();
        return List.of(
                new TestDataEntry(
                        UUID.randomUUID().toString(),
                        "정상승인 예시",
                        "ongwhtion44",
                        "승인거래",
                        "ISO0234000520200323804213060920400030000000000450000",
                        now.toString()
                ),
                new TestDataEntry(
                        UUID.randomUUID().toString(),
                        "정상취소 예시",
                        "ongwhtioj13",
                        "취소거래",
                        "ISO0234000520200323804213060920400020000000000450000",
                        now.toString()
                )
        );
    }

    private synchronized void persist() {
        try {
            Files.createDirectories(STORE_FILE.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(STORE_FILE.toFile(), new VaultStore(processes, entries));
        } catch (IOException e) {
            throw new UncheckedIOException("테스트 데이터함 파일을 저장하지 못했습니다: " + STORE_FILE, e);
        }
    }

    public synchronized List<String> listProcesses() {
        return List.copyOf(processes);
    }

    public synchronized List<String> registerProcess(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("프로세스 이름을 입력하세요.");
        }
        if (!processes.contains(trimmed)) {
            processes.add(trimmed);
            persist();
        }
        return List.copyOf(processes);
    }

    public synchronized List<TestDataEntry> listEntries(String processFilter) {
        if (processFilter == null || processFilter.isBlank()) {
            return List.copyOf(entries);
        }
        return entries.stream().filter(e -> e.processName().equals(processFilter)).toList();
    }

    public synchronized TestDataEntry save(TestDataSaveRequest request) {
        String processName = blankToDefault(request.getProcessName(), "미분류");
        if (!processes.contains(processName)) {
            processes.add(processName);
        }

        TestDataEntry entry = new TestDataEntry(
                UUID.randomUUID().toString(),
                blankToDefault(request.getName(), "(이름 없음)"),
                processName,
                blankToDefault(request.getTransactionType(), "-"),
                request.getContent() == null ? "" : request.getContent(),
                Instant.now().toString()
        );
        entries.add(0, entry);
        persist();
        return entry;
    }

    public synchronized boolean delete(String id) {
        boolean removed = entries.removeIf(e -> e.id().equals(id));
        if (removed) {
            persist();
        }
        return removed;
    }

    private String blankToDefault(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
