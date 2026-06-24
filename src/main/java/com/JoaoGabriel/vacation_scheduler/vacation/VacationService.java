package com.JoaoGabriel.vacation_scheduler.vacation;

import com.JoaoGabriel.vacation_scheduler.employee.Employee;
import com.JoaoGabriel.vacation_scheduler.vacation.dto.VacationRequest;
import com.JoaoGabriel.vacation_scheduler.vacation.dto.VacationResponse;
import com.JoaoGabriel.vacation_scheduler.vacation.exception.InvalidVacationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

        if (!request.startDate().isAfter(LocalDate.now())) {
            throw new InvalidVacationException(
                    "A data de início das férias deve ser futura"
            );
        }

        if (request.endDate().isBefore(request.startDate())) {
            throw new InvalidVacationException(
                    "A data final não pode ser anterior à data inicial"
            );
        }

        LocalDate eligibilityDate =
                employee.getAdmissionDate().plusYears(1);

        if (request.startDate().isBefore(eligibilityDate)) {
            throw new InvalidVacationException(
                    "As férias só podem começar a partir de "
                            + eligibilityDate
            );
        }

        LocalDate cycleStart = calculateCycleStart(
                employee.getAdmissionDate(),
                request.startDate()
        );

        LocalDate cycleEnd = calculateCycleEnd(cycleStart);

        if (LocalDate.now().isBefore(cycleStart)) {
            throw new InvalidVacationException(
                    "Este ciclo de férias só estará disponível a partir de "
                            + cycleStart
            );
        }

        if (request.endDate().isAfter(cycleEnd)) {
            throw new InvalidVacationException(
                    "O período de férias deve terminar até "
                            + cycleEnd
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
                vacationRepository
                        .findByEmployeeIdAndStartDateBetween(
                                employee.getId(),
                                cycleStart,
                                cycleEnd
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
        return vacationRepository
                .findByEmployeeIdOrderByStartDateAsc(employee.getId())
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

    public void delete(
            Long vacationId,
            Employee employee
    ) {
        Vacation vacation = vacationRepository
                .findByIdAndEmployeeId(
                        vacationId,
                        employee.getId()
                )
                .orElseThrow(() ->
                        new InvalidVacationException(
                                "Férias não encontradas para este funcionário"
                        )
                );

        if (!vacation.getStartDate().isAfter(LocalDate.now())) {
            throw new InvalidVacationException(
                    "Não é possível cancelar férias que já começaram ou terminaram"
            );
        }

        vacationRepository.delete(vacation);
    }
    private LocalDate calculateCycleStart(
            LocalDate admissionDate,
            LocalDate vacationStartDate
    ) {
        LocalDate cycleStart = admissionDate.plusYears(1);

        while (!vacationStartDate.isBefore(
                cycleStart.plusYears(1)
        )) {
            cycleStart = cycleStart.plusYears(1);
        }

        return cycleStart;
    }
    private LocalDate calculateCycleEnd(
            LocalDate cycleStart
    ) {
        return cycleStart
                .plusYears(1)
                .minusDays(1);
    }
}