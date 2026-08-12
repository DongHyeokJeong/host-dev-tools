package com.example.hostdevtools.stringhex;

import java.util.List;

public record StringHexConvertResponse(
        List<StringHexToken> tokens,
        List<String> warnings
) {}
