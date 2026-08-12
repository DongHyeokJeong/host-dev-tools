package com.example.hostdevtools.hostnumbering;

/**
 * 운영 데이터 엑셀 업로드 1건 (가맹점명 / 코드 / 운영 포트).
 */
public record ProductionEntry(String merchantName, String code, int port) {}
