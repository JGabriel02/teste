package com.JoaoGabriel.vacation_scheduler.vacation;

import com.JoaoGabriel.vacation_scheduler.employee.Employee;
import com.JoaoGabriel.vacation_scheduler.vacation.dto.VacationRequest;
import com.JoaoGabriel.vacation_scheduler.vacation.dto.VacationResponse;
import com.JoaoGabriel.vacation_scheduler.vacation.exception.InvalidVacationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VacationServiceTest {

    @Mock
    private VacationRepository vacationRepository;

    private VacationService vacationService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        vacationService = new VacationService(vacationRepository);

        employee = new Employee();
        employee.setId(1L);
        employee.setNome("Lucas Ferreira");
        employee.setEmail("lucas.ferreira@email.com");

        /*
         * Essa data faz com que o funcionário:
         *
         * - já tenha completado mais de um ano;
         * - esteja em um ciclo atualmente ativo;
         * - ainda tenha vários meses antes do fim do ciclo.
         */
        employee.setAdmissionDate(
                LocalDate.now()
                        .minusYears(2)
                        .minusMonths(3)
        );
    }

    @Test
    void shouldRejectVacationWithPastStartDate() {
        VacationRequest request = new VacationRequest(
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(8)
        );

        InvalidVacationException exception = assertThrows(
                InvalidVacationException.class,
                () -> vacationService.create(request, employee)
        );

        assertEquals(
                "A data de início das férias deve ser futura",
                exception.getMessage()
        );

        verify(vacationRepository, never()).save(any());
    }

    @Test
    void shouldRejectVacationStartingToday() {
        VacationRequest request = new VacationRequest(
                LocalDate.now(),
                LocalDate.now().plusDays(9)
        );

        InvalidVacationException exception = assertThrows(
                InvalidVacationException.class,
                () -> vacationService.create(request, employee)
        );

        assertEquals(
                "A data de início das férias deve ser futura",
                exception.getMessage()
        );

        verify(vacationRepository, never()).save(any());
    }

    @Test
    void shouldRejectEndDateBeforeStartDate() {
        LocalDate startDate = LocalDate.now().plusDays(10);

        VacationRequest request = new VacationRequest(
                startDate,
                startDate.minusDays(1)
        );

        InvalidVacationException exception = assertThrows(
                InvalidVacationException.class,
                () -> vacationService.create(request, employee)
        );

        assertEquals(
                "A data final não pode ser anterior à data inicial",
                exception.getMessage()
        );

        verify(vacationRepository, never()).save(any());
    }

    @Test
    void shouldRejectVacationBeforeFirstEligibilityDate() {
        Employee recentlyHiredEmployee = new Employee();
        recentlyHiredEmployee.setId(2L);
        recentlyHiredEmployee.setNome("Funcionário Novo");
        recentlyHiredEmployee.setEmail("novo@email.com");
        recentlyHiredEmployee.setAdmissionDate(
                LocalDate.now().minusMonths(6)
        );

        LocalDate startDate = LocalDate.now().plusDays(10);

        VacationRequest request = new VacationRequest(
                startDate,
                startDate.plusDays(9)
        );

        LocalDate eligibilityDate =
                recentlyHiredEmployee
                        .getAdmissionDate()
                        .plusYears(1);

        InvalidVacationException exception = assertThrows(
                InvalidVacationException.class,
                () -> vacationService.create(
                        request,
                        recentlyHiredEmployee
                )
        );

        assertEquals(
                "As férias só podem começar a partir de "
                        + eligibilityDate,
                exception.getMessage()
        );

        verify(vacationRepository, never()).save(any());
    }

    @Test
    void shouldRejectVacationFromCycleThatHasNotStartedYet() {
        LocalDate currentCycleStart = getCurrentCycleStart();
        LocalDate nextCycleStart =
                currentCycleStart.plusYears(1);

        VacationRequest request = new VacationRequest(
                nextCycleStart.plusDays(10),
                nextCycleStart.plusDays(19)
        );

        InvalidVacationException exception = assertThrows(
                InvalidVacationException.class,
                () -> vacationService.create(request, employee)
        );

        assertEquals(
                "Este ciclo de férias só estará disponível a partir de "
                        + nextCycleStart,
                exception.getMessage()
        );

        verify(vacationRepository, never()).save(any());
    }

    @Test
    void shouldRejectVacationThatCrossesAcquisitionCycle() {
        LocalDate cycleEnd = getCurrentCycleEnd();

        VacationRequest request = new VacationRequest(
                cycleEnd.minusDays(4),
                cycleEnd.plusDays(5)
        );

        InvalidVacationException exception = assertThrows(
                InvalidVacationException.class,
                () -> vacationService.create(request, employee)
        );

        assertEquals(
                "O período de férias deve terminar até "
                        + cycleEnd,
                exception.getMessage()
        );

        verify(vacationRepository, never()).save(any());
    }

    @Test
    void shouldRejectVacationWithInvalidNumberOfDays() {
        LocalDate startDate = LocalDate.now().plusDays(10);

        VacationRequest request = new VacationRequest(
                startDate,
                startDate.plusDays(4)
        );

        InvalidVacationException exception = assertThrows(
                InvalidVacationException.class,
                () -> vacationService.create(request, employee)
        );

        assertEquals(
                "O período deve ter 10, 20 ou 30 dias",
                exception.getMessage()
        );

        verify(vacationRepository, never()).save(any());
    }

    @Test
    void shouldRejectOverlappingVacation() {
        LocalDate startDate = LocalDate.now().plusDays(10);

        VacationRequest request = new VacationRequest(
                startDate,
                startDate.plusDays(9)
        );

        when(
                vacationRepository
                        .existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                request.endDate(),
                                request.startDate()
                        )
        ).thenReturn(true);

        InvalidVacationException exception = assertThrows(
                InvalidVacationException.class,
                () -> vacationService.create(request, employee)
        );

        assertEquals(
                "O período escolhido já está ocupado",
                exception.getMessage()
        );

        verify(vacationRepository, never()).save(any());
    }

    @Test
    void shouldRejectVacationWhenEmployeeExceedsThirtyDays() {
        LocalDate startDate = LocalDate.now().plusDays(10);

        VacationRequest request = new VacationRequest(
                startDate,
                startDate.plusDays(19)
        );

        Vacation existingVacation = createVacation(
                1L,
                LocalDate.now().plusDays(40),
                20
        );

        when(
                vacationRepository
                        .findByEmployeeIdAndStartDateBetween(
                                employee.getId(),
                                getCurrentCycleStart(),
                                getCurrentCycleEnd()
                        )
        ).thenReturn(List.of(existingVacation));

        InvalidVacationException exception = assertThrows(
                InvalidVacationException.class,
                () -> vacationService.create(request, employee)
        );

        assertEquals(
                "O funcionário não pode ultrapassar 30 dias de férias",
                exception.getMessage()
        );

        verify(vacationRepository, never()).save(any());
    }

    @Test
    void shouldRejectTenPlusTenDivision() {
        LocalDate startDate = LocalDate.now().plusDays(10);

        VacationRequest request = new VacationRequest(
                startDate,
                startDate.plusDays(9)
        );

        Vacation existingVacation = createVacation(
                1L,
                LocalDate.now().plusDays(40),
                10
        );

        when(
                vacationRepository
                        .findByEmployeeIdAndStartDateBetween(
                                employee.getId(),
                                getCurrentCycleStart(),
                                getCurrentCycleEnd()
                        )
        ).thenReturn(List.of(existingVacation));

        InvalidVacationException exception = assertThrows(
                InvalidVacationException.class,
                () -> vacationService.create(request, employee)
        );

        assertEquals(
                "As férias devem ser tiradas em 30 dias ou divididas em 20 e 10 dias",
                exception.getMessage()
        );

        verify(vacationRepository, never()).save(any());
    }

    @Test
    void shouldCreateThirtyDayVacationSuccessfully() {
        LocalDate startDate = LocalDate.now().plusDays(10);

        VacationRequest request = new VacationRequest(
                startDate,
                startDate.plusDays(29)
        );

        when(
                vacationRepository
                        .findByEmployeeIdAndStartDateBetween(
                                employee.getId(),
                                getCurrentCycleStart(),
                                getCurrentCycleEnd()
                        )
        ).thenReturn(List.of());

        when(vacationRepository.save(any(Vacation.class)))
                .thenAnswer(invocation -> {
                    Vacation vacation = invocation.getArgument(0);
                    vacation.setId(100L);
                    return vacation;
                });

        VacationResponse response =
                vacationService.create(request, employee);

        assertEquals(100L, response.id());
        assertEquals(startDate, response.startDate());
        assertEquals(startDate.plusDays(29), response.endDate());
        assertEquals(30, response.totalDays());
        assertEquals(employee.getId(), response.employeeId());
        assertEquals(employee.getNome(), response.employeeName());

        verify(vacationRepository).save(any(Vacation.class));
    }

    @Test
    void shouldAllowTwentyDaysAfterTenDays() {
        LocalDate startDate = LocalDate.now().plusDays(10);

        VacationRequest request = new VacationRequest(
                startDate,
                startDate.plusDays(19)
        );

        Vacation existingVacation = createVacation(
                1L,
                LocalDate.now().plusDays(40),
                10
        );

        when(
                vacationRepository
                        .findByEmployeeIdAndStartDateBetween(
                                employee.getId(),
                                getCurrentCycleStart(),
                                getCurrentCycleEnd()
                        )
        ).thenReturn(List.of(existingVacation));

        when(vacationRepository.save(any(Vacation.class)))
                .thenAnswer(invocation -> {
                    Vacation vacation = invocation.getArgument(0);
                    vacation.setId(101L);
                    return vacation;
                });

        VacationResponse response =
                vacationService.create(request, employee);

        assertEquals(20, response.totalDays());
        assertEquals(employee.getId(), response.employeeId());

        verify(vacationRepository).save(any(Vacation.class));
    }

    @Test
    void shouldAllowTenDaysAfterTwentyDays() {
        LocalDate startDate = LocalDate.now().plusDays(10);

        VacationRequest request = new VacationRequest(
                startDate,
                startDate.plusDays(9)
        );

        Vacation existingVacation = createVacation(
                1L,
                LocalDate.now().plusDays(40),
                20
        );

        when(
                vacationRepository
                        .findByEmployeeIdAndStartDateBetween(
                                employee.getId(),
                                getCurrentCycleStart(),
                                getCurrentCycleEnd()
                        )
        ).thenReturn(List.of(existingVacation));

        when(vacationRepository.save(any(Vacation.class)))
                .thenAnswer(invocation -> {
                    Vacation vacation = invocation.getArgument(0);
                    vacation.setId(102L);
                    return vacation;
                });

        VacationResponse response =
                vacationService.create(request, employee);

        assertEquals(10, response.totalDays());

        verify(vacationRepository).save(any(Vacation.class));
    }

    @Test
    void shouldListEmployeeVacations() {
        Vacation firstVacation = createVacation(
                1L,
                LocalDate.now().plusMonths(1),
                10
        );

        Vacation secondVacation = createVacation(
                2L,
                LocalDate.now().plusMonths(3),
                20
        );

        when(
                vacationRepository
                        .findByEmployeeIdOrderByStartDateAsc(
                                employee.getId()
                        )
        ).thenReturn(
                List.of(firstVacation, secondVacation)
        );

        List<VacationResponse> responses =
                vacationService.findByEmployee(employee);

        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).id());
        assertEquals(2L, responses.get(1).id());
    }

    @Test
    void shouldDeleteFutureVacationOwnedByEmployee() {
        Vacation vacation = createVacation(
                1L,
                LocalDate.now().plusDays(20),
                10
        );

        when(
                vacationRepository.findByIdAndEmployeeId(
                        vacation.getId(),
                        employee.getId()
                )
        ).thenReturn(Optional.of(vacation));

        vacationService.delete(
                vacation.getId(),
                employee
        );

        verify(vacationRepository).delete(vacation);
    }

    @Test
    void shouldRejectDeleteWhenVacationDoesNotBelongToEmployee() {
        Long vacationId = 999L;

        when(
                vacationRepository.findByIdAndEmployeeId(
                        vacationId,
                        employee.getId()
                )
        ).thenReturn(Optional.empty());

        InvalidVacationException exception = assertThrows(
                InvalidVacationException.class,
                () -> vacationService.delete(
                        vacationId,
                        employee
                )
        );

        assertEquals(
                "Férias não encontradas para este funcionário",
                exception.getMessage()
        );

        verify(vacationRepository, never())
                .delete(any(Vacation.class));
    }

    @Test
    void shouldRejectDeleteWhenVacationHasAlreadyStarted() {
        Vacation vacation = createVacation(
                1L,
                LocalDate.now(),
                10
        );

        when(
                vacationRepository.findByIdAndEmployeeId(
                        vacation.getId(),
                        employee.getId()
                )
        ).thenReturn(Optional.of(vacation));

        InvalidVacationException exception = assertThrows(
                InvalidVacationException.class,
                () -> vacationService.delete(
                        vacation.getId(),
                        employee
                )
        );

        assertEquals(
                "Não é possível cancelar férias que já começaram ou terminaram",
                exception.getMessage()
        );

        verify(vacationRepository, never())
                .delete(any(Vacation.class));
    }

    @Test
    void shouldRejectDeleteWhenVacationHasAlreadyEnded() {
        Vacation vacation = createVacation(
                1L,
                LocalDate.now().minusDays(20),
                10
        );

        when(
                vacationRepository.findByIdAndEmployeeId(
                        vacation.getId(),
                        employee.getId()
                )
        ).thenReturn(Optional.of(vacation));

        InvalidVacationException exception = assertThrows(
                InvalidVacationException.class,
                () -> vacationService.delete(
                        vacation.getId(),
                        employee
                )
        );

        assertEquals(
                "Não é possível cancelar férias que já começaram ou terminaram",
                exception.getMessage()
        );

        verify(vacationRepository, never())
                .delete(any(Vacation.class));
    }

    private LocalDate getCurrentCycleStart() {
        LocalDate cycleStart =
                employee.getAdmissionDate().plusYears(1);

        while (!LocalDate.now().isBefore(
                cycleStart.plusYears(1)
        )) {
            cycleStart = cycleStart.plusYears(1);
        }

        return cycleStart;
    }

    private LocalDate getCurrentCycleEnd() {
        return getCurrentCycleStart()
                .plusYears(1)
                .minusDays(1);
    }

    private Vacation createVacation(
            Long id,
            LocalDate startDate,
            int totalDays
    ) {
        Vacation vacation = new Vacation();

        vacation.setId(id);
        vacation.setStartDate(startDate);
        vacation.setEndDate(
                startDate.plusDays(totalDays - 1L)
        );
        vacation.setTotalDays(totalDays);
        vacation.setEmployee(employee);

        return vacation;
    }
}

