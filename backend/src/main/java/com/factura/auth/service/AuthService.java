package com.factura.auth.service;

import com.factura.auth.dto.AuthResponse;
import com.factura.auth.dto.LoginRequest;
import com.factura.auth.dto.RefreshRequest;
import com.factura.auth.dto.RegisterRequest;
import com.factura.auth.security.JwtTokenService;
import com.factura.common.exception.ApiException;
import com.factura.company.Company;
import com.factura.company.repository.CompanyRepository;
import com.factura.user.User;
import com.factura.user.UserRole;
import com.factura.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already in use");
        }
        if (companyRepository.findByTaxId(request.companyTaxId()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Company tax ID already in use");
        }

        Company company = new Company();
        company.setName(request.companyName());
        company.setTaxId(request.companyTaxId());
        companyRepository.save(company);

        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setFullName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.ADMIN);
        user.setCompany(company);
        user.setActive(true);
        userRepository.save(user);

        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = jwtTokenService.generateRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenService.getAccessTokenExpirationSeconds(),
                user.getRole().name(),
                user.getFullName()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "User is inactive");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = jwtTokenService.generateRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenService.getAccessTokenExpirationSeconds(),
                user.getRole().name(),
                user.getFullName()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshRequest request) {
        if (!jwtTokenService.isTokenValid(request.refreshToken(), "refresh")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        String email = jwtTokenService.extractSubject(request.refreshToken());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));

        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = jwtTokenService.generateRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenService.getAccessTokenExpirationSeconds(),
                user.getRole().name(),
                user.getFullName()
        );
    }
}
