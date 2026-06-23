package com.JoaoGabriel.vacation_scheduler.vacation.dto;

import java.time.LocalDate;

public record VacationResponse(
        Long id,
        LocalDate startDate,
        LocalDate endDate,
        Integer totalDays,
        Long employeeId,
        String employeeName
) {
}