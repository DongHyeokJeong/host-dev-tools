package com.example.vantools.stringhex;

import java.util.List;

public record StringHexConvertResponse(
        List<StringHexToken> tokens,
        List<String> warnings
) {}
