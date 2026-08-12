package com.example.hostdevtools.stringhex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class StringHexServiceTest {

    private final StringHexService service = new StringHexService();

    @Test
    void mixedAsciiAndLiteralExampleFromSpec() {
        // 요구사항 정의서 07번 화면 예시: BASDKJNCVLS1213(03)(00)(F2)(3C)(24)
        // -> 42 41 53 44 4B 4A 4E 43 56 4C 53 31 32 31 33 03 00 F2 3C 24
        StringHexConvertResponse result = service.convert(
                "BASDKJNCVLS1213(03)(00)(F2)(3C)(24)", "ASCII");

        String joined = result.tokens().stream()
                .map(StringHexToken::hex)
                .collect(Collectors.joining(" "));

        assertEquals("42 41 53 44 4B 4A 4E 43 56 4C 53 31 32 31 33 03 00 F2 3C 24", joined);
        assertTrue(result.warnings().isEmpty());
    }

    @Test
    void unmatchedParenIsTreatedAsLiteralCharacter() {
        StringHexConvertResponse result = service.convert("A(ZZ)B", "ASCII");

        // (ZZ) is not valid 1~2 digit hex, so '(' falls back to an ASCII character
        // and parsing continues from the next character (확인 필요 처리 정책).
        assertEquals(6, result.tokens().size());
        assertEquals("28", result.tokens().get(1).hex()); // '(' -> 0x28
        assertFalse(result.warnings().isEmpty());
    }
}
