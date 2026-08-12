package com.example.vantools.bitmap;

import java.util.List;

public record BitmapParseResponse(
        String primaryBitmapHex,
        String secondaryBitmapHex,  // 1차 비트맵의 1번 비트가 켜져 있고 데이터가 충분할 때만 값이 채워짐
        boolean hasSecondary,
        List<BitmapField> primaryFields,    // 필드 1~64
        List<BitmapField> secondaryFields,  // 필드 65~128 (2차 비트맵이 없으면 빈 목록)
        List<String> warnings
) {}
