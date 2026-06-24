package com.JoaoGabriel.vacation_scheduler.vacation;

import com.JoaoGabriel.vacation_scheduler.employee.Employee;
import com.JoaoGabriel.vacation_scheduler.vacation.dto.VacationRequest;
import com.JoaoGabriel.vacation_scheduler.vacation.dto.VacationResponse;
import com.JoaoGabriel.vacation_scheduler.vacation.exception.InvalidVacationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VacationService {

    private final VacationRepository vacationRepository;

    public VacationResponse create(
            VacationRequest request,
            Employee employee
    ) {

        if (request.endDate().isBefore(request.startDate())) {
            throw new InvalidVacationException(
                    "A data final não pode ser anterior à data inicial"
            );
        }

        int totalDays = (int) ChronoUnit.DAYS.between(
                request.startDate(),
                request.endDate()
        ) + 1;

        if (totalDays != 10
                && totalDays != 20
                && totalDays != 30) {

            throw new InvalidVacationException(
                    "O período deve ter 10, 20 ou 30 dias"
            );
        }

        boolean overlapping =
                vacationRepository
                        .existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                request.endDate(),
                                request.startDate()
                        );

        if (overlapping) {
            throw new InvalidVacationException(
                    "O período escolhido já está ocupado"
            );
        }

        List<Vacation> employeeVacations =
                vacationRepository.findByEmployeeId(
                        employee.getId()
                );

        int usedDays = employeeVacations.stream()
                .mapToInt(Vacation::getTotalDays)
                .sum();

        if (usedDays + totalDays > 30) {
            throw new InvalidVacationException(
                    "O funcionário não pode ultrapassar 30 dias de férias"
            );
        }

        if (!isValidDivision(usedDays, totalDays)) {
            throw new InvalidVacationException(
                    "As férias devem ser tiradas em 30 dias ou divididas em 20 e 10 dias"
            );
        }

        Vacation vacation = new Vacation();
        vacation.setStartDate(request.startDate());
        vacation.setEndDate(request.endDate());
        vacation.setTotalDays(totalDays);
        vacation.setEmployee(employee);

        Vacation savedVacation =
                vacationRepository.save(vacation);

        return new VacationResponse(
                savedVacation.getId(),
                savedVacation.getStartDate(),
                savedVacation.getEndDate(),
                savedVacation.getTotalDays(),
                savedVacation.getEmployee().getId(),
                savedVacation.getEmployee().getNome()
        );
    }

    private boolean isValidDivision(
            int usedDays,
            int requestedDays
    ) {

        if (usedDays == 0) {
            return requestedDays == 10
                    || requestedDays == 20
                    || requestedDays == 30;
        }

        if (usedDays == 10) {
            return requestedDays == 20;
        }

        if (usedDays == 20) {
            return requestedDays == 10;
        }

        return false;
    }

    public List<VacationResponse> findByEmployee(Employee employee) {
        return vacationRepository.findByEmployeeId(employee.getId())
                .stream()
                .map(vacation -> new VacationResponse(
                        vacation.getId(),
                        vacation.getStartDate(),
                        vacation.getEndDate(),
                        vacation.getTotalDays(),
                        vacation.getEmployee().getId(),
                        vacation.getEmployee().getNome()
                ))
                .toList();
    }
}