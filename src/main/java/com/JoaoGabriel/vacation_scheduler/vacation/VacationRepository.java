package com.JoaoGabriel.vacation_scheduler.vacation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface VacationRepository
        extends JpaRepository<Vacation, Long> {

    List<Vacation> findByEmployeeId(Long employeeId);

    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate endDate,
            LocalDate startDate
    );
}