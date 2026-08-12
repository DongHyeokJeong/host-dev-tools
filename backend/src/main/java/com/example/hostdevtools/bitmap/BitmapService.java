package com.example.hostdevtools.bitmap;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

/**
 * "비트맵 파서" 화면의 파싱 로직.
 *
 * 16자리(1차) 또는 32자리(1차+2차) Hex 문자열을 받아 각 비트(필드 번호 1~64, 필요 시 65~128)의
 * On/Off를 계산한다. 1차 비트맵의 1번 비트는 관례상 2차 비트맵 존재 여부를 나타낸다.
 */
@Service
public class BitmapService {

    private static final Pattern BITMAP_HEX = Pattern.compile("^[0-9A-Fa-f]{16}$");
    private static final int BITMAP_HEX_LENGTH = 16;

    public BitmapParseResponse parse(String rawInput) {
        String input = rawInput == null ? "" : rawInput.trim();
        List<String> warnings = new ArrayList<>();

        if (input.length() < BITMAP_HEX_LENGTH) {
            warnings.add(String.format("1차 비트맵은 %d자리 Hex여야 하는데 입력이 %d자뿐입니다.",
                    BITMAP_HEX_LENGTH, input.length()));
            return new BitmapParseResponse(input, null, false, List.of(), List.of(), warnings);
        }

        String primaryBitmapHex = input.substring(0, BITMAP_HEX_LENGTH);
        if (!BITMAP_HEX.matcher(primaryBitmapHex).matches()) {
            warnings.add(String.format("1차 비트맵 '%s'가 유효한 16자리 Hex 값이 아닙니다.", primaryBitmapHex));
            return new BitmapParseResponse(primaryBitmapHex, null, false, List.of(), List.of(), warnings);
        }

        List<BitmapField> primaryFields = decodeBitmap(primaryBitmapHex, 1);
        boolean hasSecondary = primaryFields.get(0).on();

        String secondaryBitmapHex = null;
        List<BitmapField> secondaryFields = List.of();
        if (hasSecondary) {
            String rest = input.substring(BITMAP_HEX_LENGTH);
            if (rest.length() < BITMAP_HEX_LENGTH) {
                warnings.add("1번 비트가 켜져 있어 2차 비트맵이 있어야 하지만, 이어지는 데이터가 부족합니다.");
            } else {
                secondaryBitmapHex = rest.substring(0, BITMAP_HEX_LENGTH);
                if (!BITMAP_HEX.matcher(secondaryBitmapHex).matches()) {
                    warnings.add(String.format("2차 비트맵 '%s'가 유효한 16자리 Hex 값이 아닙니다.", secondaryBitmapHex));
                    secondaryBitmapHex = null;
                } else {
                    secondaryFields = decodeBitmap(secondaryBitmapHex, 65);
                }
            }
        }

        return new BitmapParseResponse(primaryBitmapHex, secondaryBitmapHex, hasSecondary,
                primaryFields, secondaryFields, warnings);
    }

    private List<BitmapField> decodeBitmap(String hex, int startFieldNo) {
        List<BitmapField> fields = new ArrayList<>();
        for (int i = 0; i < hex.length(); i++) {
            int nibble = Character.digit(hex.charAt(i), 16);
            for (int b = 0; b < 4; b++) {
                int fieldNo = startFieldNo + i * 4 + b;
                boolean set = (nibble & (0x8 >> b)) != 0;
                fields.add(new BitmapField(fieldNo, set));
            }
        }
        return fields;
    }
}
