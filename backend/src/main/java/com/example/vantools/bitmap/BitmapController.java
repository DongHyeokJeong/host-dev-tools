package com.example.vantools.bitmap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bitmap")
public class BitmapController {

    private final BitmapService service;

    public BitmapController(BitmapService service) {
        this.service = service;
    }

    @PostMapping("/parse")
    public ResponseEntity<BitmapParseResponse> parse(@RequestBody BitmapParseRequest request) {
        String input = request.getInput() == null ? "" : request.getInput();
        return ResponseEntity.ok(service.parse(input));
    }
}
