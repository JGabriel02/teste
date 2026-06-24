package com.JoaoGabriel.vacation_scheduler.vacation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VacationRepository extends JpaRepository<Vacation, Long> {

    List<Vacation> findByEmployeeIdOrderByStartDateAsc(Long employeeId);

    List<Vacation> findByEmployeeIdAndStartDateBetween(
            Long employeeId,
            LocalDate cycleStart,
            LocalDate cycleEnd
    );

    Optional<Vacation> findByIdAndEmployeeId(
            Long id,
            Long employeeId
    );

    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate endDate,
            LocalDate startDate
    );

    List<Vacation> findByEmployeeManagerIdOrderByStartDateAsc(
            Long managerId
    );
}