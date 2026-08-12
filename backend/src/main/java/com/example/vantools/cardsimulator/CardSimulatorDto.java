package com.example.vantools.cardsimulator;

public record CardSimulatorDto(
        String id,
        String name,
        String status,       // "RUNNING" | "STOPPED"
        String lastActionAt  // ISO-8601 시각
) {}
