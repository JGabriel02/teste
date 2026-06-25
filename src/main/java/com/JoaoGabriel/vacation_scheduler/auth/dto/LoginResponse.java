package com.JoaoGabriel.vacation_scheduler.auth.dto;

import com.JoaoGabriel.vacation_scheduler.employee.EmployeeRole;

public record LoginResponse(
        Long id,
        String nome,
        String email,
        EmployeeRole role,
        String managerCode,
        String token

) {
}