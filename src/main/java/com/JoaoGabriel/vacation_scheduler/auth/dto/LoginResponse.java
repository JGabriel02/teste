package com.JoaoGabriel.vacation_scheduler.auth.dto;

public record LoginResponse(
        Long id,
        String nome,
        String email
) {
}