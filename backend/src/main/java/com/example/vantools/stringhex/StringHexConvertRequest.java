package com.example.vantools.stringhex;

public class StringHexConvertRequest {

    private String input;
    private String encoding; // "ASCII" | "EBCDIC" — 확인 필요: 화면의 "ASCII 변환" 체크박스와의 정확한 매핑

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
