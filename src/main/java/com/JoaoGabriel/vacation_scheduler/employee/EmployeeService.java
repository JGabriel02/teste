package com.JoaoGabriel.vacation_scheduler.employee;

import com.JoaoGabriel.vacation_scheduler.employee.dto.EmployeeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public Employee create(EmployeeRequest request){
        Employee employee = new Employee();

        employee.setNome(request.nome());
        employee.setEmail(request.email());
        employee.setPassword(request.password());

        return employeeRepository.save(employee);
    }
}
