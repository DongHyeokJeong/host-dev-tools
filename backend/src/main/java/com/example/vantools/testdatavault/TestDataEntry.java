package com.example.vantools.testdatavault;

public record TestDataEntry(
        String id,
        String name,             // 저장 라벨 (예: "정상승인 케이스")
        String processName,      // 프로세스 코드 (예: "ongwhtion44", "ongwhtioj13")
        String transactionType,  // 거래 구분 (예: "승인거래", "취소거래")
        String content,          // 저장된 원문 전문
        String createdAt         // ISO-8601 시각
) {}
