package com.example.hostdevtools.isoparsing;

/**
 * 필드 정의표(IsoParsingService.FIELD_SPECS) 한 항목의 파싱 결과.
 *   length가 null이면 아직 길이가 정의되지 않은 필드(확인 필요)라는 뜻이고, value도 null입니다.
 *   complete는 정의된 길이만큼 데이터를 온전히 읽었는지 여부입니다.
 */
public record IsoParsedField(
        String name,
        int offset,  // 원문 기준 시작 위치(0-base). 길이 미정 필드는 직전 필드가 끝난 위치를 그대로 가짐
        String value,
        Integer length,
        boolean complete
) {}
