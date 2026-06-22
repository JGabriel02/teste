package com.JoaoGabriel.vacation_scheduler.employee;

import com.JoaoGabriel.vacation_scheduler.employee.dto.EmployeeRequest;
import com.JoaoGabriel.vacation_scheduler.employee.dto.EmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.JoaoGabriel.vacation_scheduler.employee.exception.EmailAlreadyExistsException;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;


    public EmployeeResponse create(EmployeeRequest request) {

        if (employeeRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException("E-mail já cadastrado");
        }

        Employee employee = new Employee();

        employee.setNome(request.nome());
        employee.setEmail(request.email());
        employee.setPassword(
                passwordEncoder.encode(request.password())
        );

        Employee savedEmployee = employeeRepository.save(employee);

        return new EmployeeResponse(
                savedEmployee.getId(),
                savedEmployee.getNome(),
                savedEmployee.getEmail()
        );
    }
}