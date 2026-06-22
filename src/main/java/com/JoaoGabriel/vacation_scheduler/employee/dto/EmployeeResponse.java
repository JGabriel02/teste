package com.JoaoGabriel.vacation_scheduler.employee.dto;

public record EmployeeResponse(
        Long id,
        String nome,
        String email
) {
}