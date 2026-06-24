package com.JoaoGabriel.vacation_scheduler.employee;

import com.JoaoGabriel.vacation_scheduler.vacation.Vacation;
import com.JoaoGabriel.vacation_scheduler.vacation.VacationRepository;
import com.JoaoGabriel.vacation_scheduler.vacation.dto.VacationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final VacationRepository vacationRepository;

    public List<VacationResponse> listEmployeeVacations(
            Employee authenticatedEmployee
    ) {
        if (authenticatedEmployee == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Autenticação necessária"
            );
        }

        if (authenticatedEmployee.getRole() != EmployeeRole.MANAGER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Apenas gestores podem acessar esta rota"
            );
        }

        return vacationRepository
                .findByEmployeeManagerIdOrderByStartDateAsc(
                        authenticatedEmployee.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private VacationResponse toResponse(Vacation vacation) {
        return new VacationResponse(
                vacation.getId(),
                vacation.getStartDate(),
                vacation.getEndDate(),
                vacation.getTotalDays(),
                vacation.getEmployee().getId(),
                vacation.getEmployee().getNome()
        );
    }
}

