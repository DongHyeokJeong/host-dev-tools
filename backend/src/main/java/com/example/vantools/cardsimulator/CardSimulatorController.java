package com.example.vantools.cardsimulator;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/card-simulators")
public class CardSimulatorController {

    private final CardSimulatorService service;

    public CardSimulatorController(CardSimulatorService service) {
        this.service = service;
    }

    @GetMapping
    public List<CardSimulatorDto> list() {
        return service.list();
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<CardSimulatorDto> start(@PathVariable String id) {
        return respond(() -> service.start(id));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<CardSimulatorDto> stop(@PathVariable String id) {
        return respond(() -> service.stop(id));
    }

    @PostMapping("/{id}/restart")
    public ResponseEntity<CardSimulatorDto> restart(@PathVariable String id) {
        return respond(() -> service.restart(id));
    }

    private ResponseEntity<CardSimulatorDto> respond(Supplier<CardSimulatorDto> action) {
        try {
            return ResponseEntity.ok(action.get());
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
