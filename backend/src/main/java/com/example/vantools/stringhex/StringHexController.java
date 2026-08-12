package com.example.vantools.stringhex;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/string-hex")
public class StringHexController {

    private final StringHexService service;

    public StringHexController(StringHexService service) {
        this.service = service;
    }

    @PostMapping("/convert")
    public ResponseEntity<StringHexConvertResponse> convert(@RequestBody StringHexConvertRequest request) {
        String encoding = request.getEncoding() == null ? "ASCII" : request.getEncoding();
        String input = request.getInput() == null ? "" : request.getInput();
        return ResponseEntity.ok(service.convert(input, encoding));
    }
}
