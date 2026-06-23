package com.JoaoGabriel.vacation_scheduler.auth;

import com.JoaoGabriel.vacation_scheduler.auth.dto.LoginRequest;
import com.JoaoGabriel.vacation_scheduler.auth.dto.LoginResponse;
import com.JoaoGabriel.vacation_scheduler.auth.exception.InvalidCredentialsException;
import com.JoaoGabriel.vacation_scheduler.employee.Employee;
import com.JoaoGabriel.vacation_scheduler.employee.EmployeeRepository;
import com.JoaoGabriel.vacation_scheduler.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {

        Employee employee = employeeRepository
                .findByEmail(request.email())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "E-mail ou senha inválidos"
                        )
                );

        boolean passwordMatches = passwordEncoder.matches(
                request.password(),
                employee.getPassword()
        );

        if (!passwordMatches) {
            throw new InvalidCredentialsException(
                    "E-mail ou senha inválidos"
            );
        }

        String token = jwtService.generateToken(
                employee.getEmail()
        );

        return new LoginResponse(
                employee.getId(),
                employee.getNome(),
                employee.getEmail(),
                token
        );
    }
}