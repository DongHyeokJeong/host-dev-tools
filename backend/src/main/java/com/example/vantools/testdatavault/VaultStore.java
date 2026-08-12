package com.example.vantools.testdatavault;

import java.util.List;

/**
 * 파일(data/test-data-vault.json)에 그대로 직렬화/역직렬화되는 저장 구조체.
 */
public record VaultStore(List<String> processes, List<TestDataEntry> entries) {}
