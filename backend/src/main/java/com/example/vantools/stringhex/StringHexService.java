package com.example.vantools.stringhex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

/**
 * "String -> 16진수 변환" 화면(07_String_Hex변환, B안)의 파싱/변환 로직.
 *
 * 입력 문자열은 일반 ASCII 문자와 (XX) 형태의 Hex 리터럴이 섞여 들어올 수 있다.
 *   - 일반 문자: 코드값으로 변환 (예: 'B' -> 42)
 *   - (XX) 리터럴: 괄호를 벗기고 값을 그대로 사용 (예: (03) -> 03)
 */
@Service
public class StringHexService {

    private static final Pattern HEX_LITERAL_BODY = Pattern.compile("^[0-9A-Fa-f]{1,2}$");

    // 확인 필요: 아래 EBCDIC 표는 IBM CP037 계열을 참고한 근사치입니다.
    // VAN/카드사·호스트마다 실제 사용하는 EBCDIC 변형(코드페이지)이 다를 수 있어
    // 특수문자(중괄호/대괄호/역슬래시 등) 매핑은 실제 대상 호스트 규격과 대조 확인이 필요합니다.
    private static final Map<Character, Integer> ASCII_TO_EBCDIC = buildEbcdicTable();
    private static final Map<Integer, Character> EBCDIC_TO_ASCII = buildReverseTable(ASCII_TO_EBCDIC);

    public StringHexConvertResponse convert(String input, String encoding) {
        List<StringHexToken> tokens = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        boolean useEbcdic = "EBCDIC".equalsIgnoreCase(encoding);

        int len = input.length();
        int i = 0;
        while (i < len) {
            char c = input.charAt(i);

            if (c == '(') {
                int close = input.indexOf(')', i + 1);
                if (close != -1) {
                    String inner = input.substring(i + 1, close);
                    if (HEX_LITERAL_BODY.matcher(inner).matches()) {
                        String hex = (inner.length() == 1 ? "0" + inner : inner).toUpperCase();
                        String decoded = decodeChar(Integer.parseInt(hex, 16), useEbcdic);
                        tokens.add(new StringHexToken("LITERAL", i, close + 1, input.substring(i, close + 1), hex, decoded));
                        i = close + 1;
                        continue;
                    }
                    // 확인 필요: 괄호 안 값이 1~2자리 Hex가 아닐 때의 처리 정책이 요구사항에 명시되어 있지 않음.
                    // 우선 에러로 중단하지 않고 '(' 자체를 일반 문자로 취급해 계속 진행함.
                    warnings.add(String.format(
                            "위치 %d: '(%s)'는 유효한 Hex 리터럴(1~2자리)이 아니라 일반 문자로 처리했습니다.", i, inner));
                } else {
                    // 확인 필요: 닫히지 않은 '(' 처리 정책 미정. 일반 문자로 취급.
                    warnings.add(String.format("위치 %d: 닫히지 않은 '('를 일반 문자로 처리했습니다.", i));
                }
            }

            int code = useEbcdic ? toEbcdic(c, i, warnings) : asciiCode(c, i, warnings);
            String hex = String.format("%02X", code & 0xFF);
            String decoded = decodeChar(code & 0xFF, useEbcdic);
            tokens.add(new StringHexToken("ASCII", i, i + 1, String.valueOf(c), hex, decoded));
            i++;
        }

        return new StringHexConvertResponse(tokens, warnings);
    }

    // hex 값을 선택된 인코딩으로 역변환해 출력 가능한 ASCII 문자로 돌려준다.
    // 매핑이 없거나 화면에 표시할 수 없는(제어문자 등) 값은 "."으로 표시한다.
    private String decodeChar(int code, boolean useEbcdic) {
        Character ch = useEbcdic ? EBCDIC_TO_ASCII.get(code) : (char) code;
        if (ch == null || ch < 0x20 || ch > 0x7E) {
            return ".";
        }
        return String.valueOf(ch);
    }

    private int asciiCode(char c, int pos, List<String> warnings) {
        if (c > 0xFF) {
            // 확인 필요: 2바이트 이상 문자(한글 등) 입력 시 정책 미정. 우선 하위 1바이트만 사용.
            warnings.add(String.format("위치 %d: '%c'는 1바이트 범위를 벗어나 하위 바이트만 사용했습니다.", pos, c));
        }
        return c & 0xFF;
    }

    private int toEbcdic(char c, int pos, List<String> warnings) {
        Integer v = ASCII_TO_EBCDIC.get(c);
        if (v == null) {
            warnings.add(String.format(
                    "위치 %d: '%c'에 대한 EBCDIC 매핑이 없어 ASCII 코드값을 대신 사용했습니다. (확인 필요)", pos, c));
            return c & 0xFF;
        }
        return v;
    }

    private static Map<Character, Integer> buildEbcdicTable() {
        Map<Character, Integer> map = new HashMap<>();

        map.put(' ', 0x40);
        map.put('!', 0x5A);
        map.put('"', 0x7F);
        map.put('#', 0x7B);
        map.put('$', 0x5B);
        map.put('%', 0x6C);
        map.put('&', 0x50);
        map.put('\'', 0x7D);
        map.put('(', 0x4D);
        map.put(')', 0x5D);
        map.put('*', 0x5C);
        map.put('+', 0x4E);
        map.put(',', 0x6B);
        map.put('-', 0x60);
        map.put('.', 0x4B);
        map.put('/', 0x61);
        map.put(':', 0x7A);
        map.put(';', 0x5E);
        map.put('<', 0x4C);
        map.put('=', 0x7E);
        map.put('>', 0x6E);
        map.put('?', 0x6F);
        map.put('@', 0x7C);
        map.put('[', 0xAD); // 확인 필요: 코드페이지별 상이
        map.put('\\', 0xE0);
        map.put(']', 0xBD); // 확인 필요: 코드페이지별 상이
        map.put('^', 0x5F);
        map.put('_', 0x6D);
        map.put('`', 0x79);
        map.put('{', 0xC0);
        map.put('|', 0x6A);
        map.put('}', 0xD0);
        map.put('~', 0xA1);

        int[] digits = {0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9};
        for (int d = 0; d <= 9; d++) {
            map.put((char) ('0' + d), digits[d]);
        }

        int[] upperA_I = {0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9};
        for (int k = 0; k < upperA_I.length; k++) {
            map.put((char) ('A' + k), upperA_I[k]);
        }
        int[] upperJ_R = {0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9};
        for (int k = 0; k < upperJ_R.length; k++) {
            map.put((char) ('J' + k), upperJ_R[k]);
        }
        int[] upperS_Z = {0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9};
        for (int k = 0; k < upperS_Z.length; k++) {
            map.put((char) ('S' + k), upperS_Z[k]);
        }

        int[] lowerA_I = {0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89};
        for (int k = 0; k < lowerA_I.length; k++) {
            map.put((char) ('a' + k), lowerA_I[k]);
        }
        int[] lowerJ_R = {0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99};
        for (int k = 0; k < lowerJ_R.length; k++) {
            map.put((char) ('j' + k), lowerJ_R[k]);
        }
        int[] lowerS_Z = {0xA2, 0xA3, 0xA4, 0xA5, 0xA6, 0xA7, 0xA8, 0xA9};
        for (int k = 0; k < lowerS_Z.length; k++) {
            map.put((char) ('s' + k), lowerS_Z[k]);
        }

        return map;
    }

    private static Map<Integer, Character> buildReverseTable(Map<Character, Integer> asciiToEbcdic) {
        Map<Integer, Character> reverse = new HashMap<>();
        for (Map.Entry<Character, Integer> entry : asciiToEbcdic.entrySet()) {
            reverse.putIfAbsent(entry.getValue(), entry.getKey());
        }
        return reverse;
    }
}
