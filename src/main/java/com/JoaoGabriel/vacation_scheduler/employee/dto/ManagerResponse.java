package com.JoaoGabriel.vacation_scheduler.employee.dto;

import com.JoaoGabriel.vacation_scheduler.employee.EmployeeRole;

public record ManagerResponse(
        Long id,
        String nome,
        String email,
        EmployeeRole role,
        String managerCode
) {
}

