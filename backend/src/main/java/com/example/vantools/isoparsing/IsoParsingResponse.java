package com.example.vantools.isoparsing;

import java.util.List;

public record IsoParsingResponse(
        List<IsoParsedField> fields,
        String remainingData,  // 정의된 필드들을 모두 읽고 남은 원문 데이터
        List<String> warnings
) {}
