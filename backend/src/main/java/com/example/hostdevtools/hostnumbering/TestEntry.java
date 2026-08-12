package com.example.hostdevtools.hostnumbering;

/**
 * 테스트 DB에 이미 존재하는 코드/포트 1건.
 *
 * 확인 필요: 실제 테스트 DB 연동 전이라 서비스 시작 시 예시 데이터로 seed된 Mock이다.
 * 실제로는 서버에서 테스트 DB에 직접 쿼리해 이 목록을 채워야 한다.
 */
public record TestEntry(String code, int port) {}
