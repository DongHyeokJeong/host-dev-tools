package com.example.vantools.cardsimulator;

import java.time.Instant;

class CardSimulatorState {
    final String id;
    final String name;
    CardSimulatorStatus status;
    Instant lastActionAt;

    CardSimulatorState(String id, String name, CardSimulatorStatus status) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.lastActionAt = Instant.now();
    }
}
