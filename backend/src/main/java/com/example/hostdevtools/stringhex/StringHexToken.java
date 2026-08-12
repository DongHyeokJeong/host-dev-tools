package com.example.hostdevtools.stringhex;

/**
 * 입력 문자열을 파싱한 하나의 토큰(ASCII 한 글자 또는 (XX) Hex 리터럴 하나)을 나타냅니다.
 * sourceStart/sourceEnd는 원본 입력 문자열 기준 [start, end) 인덱스입니다.
 */
public record StringHexToken(
        String type,        // "ASCII" | "LITERAL"
        int sourceStart,
        int sourceEnd,
        String sourceText,
        String hex,
        String decodedChar  // hex 값을 선택된 인코딩(ASCII/EBCDIC)으로 되돌린 문자. 출력 불가 시 "."
) {}
