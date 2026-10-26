package com.campuspulse.service;

import com.campuspulse.dto.AuthResponse;
import com.campuspulse.dto.LoginRequest;
import com.campuspulse.dto.RegisterRequest;
import com.campuspulse.entity.Role;
import com.campuspulse.entity.User;
import com.campuspulse.exception.BadRequestException;
import com.campuspulse.repository.UserRepository;
import com.campuspulse.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmailOrId(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmailOrId())
                .or(() -> userRepository.findByStudentOrStaffId(loginRequest.getEmailOrId()))
                .orElseThrow(() -> new BadRequestException("User not found"));

        String jwt = jwtUtils.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getStudentOrStaffId(),
                user.getRole().name(),
                user.getDepartment()
        );
    }

    @Transactional
    public AuthResponse registerStudent(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("An account with this email already exists.");
        }
        if (userRepository.existsByStudentOrStaffId(registerRequest.getStudentId())) {
            throw new BadRequestException("An account with this Student ID already exists.");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail().trim().toLowerCase());
        user.setFullName(registerRequest.getFullName().trim());
        user.setStudentOrStaffId(registerRequest.getStudentId().trim());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.ROLE_STUDENT); // STRICT: Students can only self-register as ROLE_STUDENT
        user.setDepartment(registerRequest.getDepartment() != null ? registerRequest.getDepartment().trim() : "General");

        User saved = userRepository.save(user);

        String jwt = jwtUtils.generateToken(saved.getEmail(), saved.getRole().name());

        return new AuthResponse(
                jwt,
                saved.getId(),
                saved.getEmail(),
                saved.getFullName(),
                saved.getStudentOrStaffId(),
                saved.getRole().name(),
                saved.getDepartment()
        );
    }

    public User getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BadRequestException("User is not authenticated");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .or(() -> userRepository.findByStudentOrStaffId(email))
                .orElseThrow(() -> new BadRequestException("User profile not found for " + email));
    }
}
