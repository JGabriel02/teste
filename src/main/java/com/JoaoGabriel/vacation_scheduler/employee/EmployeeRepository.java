package com.JoaoGabriel.vacation_scheduler.employee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Employee> findByManagerCode(String managerCode);

    boolean existsByManagerCode(String managerCode);
}