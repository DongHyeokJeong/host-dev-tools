package com.example.hostdevtools.isoparsing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * "ISO 전문 파싱" 화면의 파싱 로직.
 *
 * 사용자가 준 실제 예시를 기준으로 역산한 고정 순서/길이 필드표(FIELD_SPECS)를 따라
 * 앞에서부터 순서대로 잘라낸다.
 *   예시: ISO023400052 0200 3238042130609204 00030 000000000450000 ...
 *        HEADER(12)   전문구분(4) 비트맵(16)  거래구분(5) 거래금액(15)  이후 미정
 *
 * 확인 필요: 전송일시/추적번호/개시시간/개시일자/입력 유형/거래고유번호는 아직 길이가 정의되지
 * 않아 FIELD_SPECS에 length=null로 자리만 잡아두었다. 금융결제원 스펙이 정리되면
 * 아래 목록에 길이만 채워 넣으면 이어서 파싱되도록 만들어 둔 골격이다.
 */
@Service
public class IsoParsingService {

    private record FieldSpec(String name, Integer length) {}

    private static final List<FieldSpec> FIELD_SPECS = List.of(
            new FieldSpec("HEADER", 12),
            new FieldSpec("전문구분", 4),
            new FieldSpec("비트맵", 16),
            new FieldSpec("거래구분", 5),
            new FieldSpec("거래금액", 15),
            new FieldSpec("전송일시", null),
            new FieldSpec("추적번호", null),
            new FieldSpec("개시시간", null),
            new FieldSpec("개시일자", null),
            new FieldSpec("입력 유형", null),
            new FieldSpec("거래고유번호", null)
    );

    public IsoParsingResponse parse(String rawInput) {
        String input = rawInput == null ? "" : rawInput;
        List<String> warnings = new ArrayList<>();
        List<IsoParsedField> fields = new ArrayList<>();
        List<String> undefinedFieldNames = new ArrayList<>();

        int pos = 0;
        for (FieldSpec spec : FIELD_SPECS) {
            if (spec.length() == null) {
                fields.add(new IsoParsedField(spec.name(), pos, null, null, false));
                undefinedFieldNames.add(spec.name());
                continue;
            }

            if (pos >= input.length()) {
                fields.add(new IsoParsedField(spec.name(), pos, null, spec.length(), false));
                warnings.add(String.format("'%s' 필드를 읽기 전에 입력이 끝났습니다.", spec.name()));
                continue;
            }

            int end = Math.min(pos + spec.length(), input.length());
            String value = input.substring(pos, end);
            boolean complete = (end - pos) == spec.length();
            if (!complete) {
                warnings.add(String.format("'%s' 필드는 %d자여야 하지만 %d자만 남아 있어 끝까지 읽지 못했습니다.",
                        spec.name(), spec.length(), end - pos));
            }
            fields.add(new IsoParsedField(spec.name(), pos, value, spec.length(), complete));
            pos = end;
        }

        String remainingData = pos < input.length() ? input.substring(pos) : "";

        if (!undefinedFieldNames.isEmpty()) {
            warnings.add("확인 필요: " + String.join(", ", undefinedFieldNames)
                    + " 필드는 길이가 아직 정의되지 않아 파싱하지 않았습니다. 남은 데이터는 '이후 데이터'에 그대로 표시됩니다.");
        }

        return new IsoParsingResponse(fields, remainingData, warnings);
    }
}
