package com.example.vantools.isoparsing;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/iso-parsing")
public class IsoParsingController {

    private final IsoParsingService service;

    public IsoParsingController(IsoParsingService service) {
        this.service = service;
    }

    @PostMapping("/parse")
    public ResponseEntity<IsoParsingResponse> parse(@RequestBody IsoParsingRequest request) {
        String input = request.getInput() == null ? "" : request.getInput();
        return ResponseEntity.ok(service.parse(input));
    }
}
