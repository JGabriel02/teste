package com.JoaoGabriel.vacation_scheduler.vacation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record VacationRequest(

        @NotNull(message = "A data de início é obrigatória")
        LocalDate startDate,

        @NotNull(message = "A data de fim é obrigatória")
        LocalDate endDate

) {
}