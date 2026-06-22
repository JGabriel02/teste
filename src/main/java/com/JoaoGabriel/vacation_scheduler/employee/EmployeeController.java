package com.JoaoGabriel.vacation_scheduler.employee;

import com.JoaoGabriel.vacation_scheduler.employee.dto.EmployeeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public Employee create(@Valid @RequestBody EmployeeRequest request) {
        return employeeService.create(request);
    }
}