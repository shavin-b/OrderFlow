package com.orderflow.service;

import com.orderflow.dto.auth.*;
import com.orderflow.entity.*;
import com.orderflow.entity.Role.RoleName;
import com.orderflow.entity.User.UserStatus;
import com.orderflow.exception.BusinessException;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.mapper.UserMapper;
import com.orderflow.repository.RoleRepository;
import com.orderflow.repository.UserRepository;
import com.orderflow.repository.UserSessionRepository;
import com.orderflow.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserSessionRepository sessionRepository;
    private final SubscriptionService subscriptionService;
    private final AuditLogService auditLogService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email address is already registered");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build()));

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .status(UserStatus.ACTIVE)
                .build();

        user.getRoles().add(userRole);
        User savedUser = userRepository.save(user);

        // Create 14-day free trial subscription
        subscriptionService.createTrialSubscription(savedUser);

        auditLogService.log(savedUser, "REGISTER", "User", "Registered new user account", ipAddress);

        return createAuthTokens(savedUser, ipAddress, "Register");
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByEmailWithRolesAndSubscriptions(request.getEmail())
                .orElseThrow(() -> new BusinessException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            auditLogService.log(user, "LOGIN_FAILED", "User", "Failed login attempt (wrong password)", ipAddress);
            throw new BusinessException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Account is inactive or suspended");
        }

        auditLogService.log(user, "LOGIN_SUCCESS", "User", "Successful user login", ipAddress);

        return createAuthTokens(user, ipAddress, "Login");
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress) {
        String token = request.getRefreshToken();
        if (!jwtProvider.validateToken(token)) {
            throw new BusinessException("Invalid or expired refresh token");
        }

        UserSession session = sessionRepository.findByRefreshToken(token)
                .orElseThrow(() -> new BusinessException("Refresh token session not found"));

        if (Boolean.TRUE.equals(session.getRevoked()) || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Refresh token has expired or been revoked");
        }

        User user = session.getUser();
        session.setRevoked(true); // Revoke old refresh token (Rotate)
        sessionRepository.save(session);

        auditLogService.log(user, "REFRESH_TOKEN", "UserSession", "Refreshed access token", ipAddress);

        return createAuthTokens(user, ipAddress, "RefreshToken");
    }

    @Transactional
    public void logout(String refreshToken, String ipAddress) {
        sessionRepository.findByRefreshToken(refreshToken).ifPresent(session -> {
            session.setRevoked(true);
            sessionRepository.save(session);
            auditLogService.log(session.getUser(), "LOGOUT", "UserSession", "Logged out session", ipAddress);
        });
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            user.setResetToken(UUID.randomUUID().toString());
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(2)); // Token valid for 2 hours
            userRepository.save(user);
            log.info("Password reset token generated for user email={}", user.getEmail());
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getResetToken())
                .orElseThrow(() -> new BusinessException("Invalid or expired password reset token"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Password reset token has expired");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        auditLogService.log(user, "RESET_PASSWORD", "User", "Successfully reset password", null);
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BusinessException("Invalid verification token"));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        auditLogService.log(user, "VERIFY_EMAIL", "User", "Email address verified", null);
    }

    public UserProfileDto getProfile(String email) {
        User user = userRepository.findByEmailWithRolesAndSubscriptions(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        UserProfileDto dto = userMapper.toDto(user);
        subscriptionService.getSubscriptionForUser(user.getId());
        return dto;
    }

    @Transactional
    public UserProfileDto updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        User saved = userRepository.save(user);

        return userMapper.toDto(saved);
    }

    @Transactional
    public void updatePassword(String email, UpdatePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditLogService.log(user, "UPDATE_PASSWORD", "User", "Updated account password", null);
    }

    private AuthResponse createAuthTokens(User user, String ipAddress, String userAgent) {
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.getName().name()))
                .toList();

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .build();

        String accessToken = jwtProvider.generateAccessToken(userDetails, user.getId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        // Save session
        UserSession session = UserSession.builder()
                .user(user)
                .refreshToken(refreshToken)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        sessionRepository.save(session);

        List<String> roleNames = user.getRoles().stream().map(r -> r.getName().name()).toList();
        String subStatus = subscriptionService.getSubscriptionForUser(user.getId()).getStatus().name();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .roles(roleNames)
                .subscriptionStatus(subStatus)
                .build();
    }
}
