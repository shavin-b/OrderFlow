package com.orderflow.service;

import com.orderflow.dto.auth.AuthResponse;
import com.orderflow.dto.auth.LoginRequest;
import com.orderflow.dto.auth.RegisterRequest;
import com.orderflow.dto.auth.SubscriptionDto;
import com.orderflow.entity.Role;
import com.orderflow.entity.Role.RoleName;
import com.orderflow.entity.Subscription.SubscriptionStatus;
import com.orderflow.entity.User;
import com.orderflow.exception.BusinessException;
import com.orderflow.mapper.UserMapper;
import com.orderflow.repository.RoleRepository;
import com.orderflow.repository.UserRepository;
import com.orderflow.repository.UserSessionRepository;
import com.orderflow.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserSessionRepository sessionRepository;
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private User user;
    private Role roleUser;

    @BeforeEach
    void setUp() {
        roleUser = Role.builder().id(1L).name(RoleName.ROLE_USER).build();
        user = User.builder()
                .id(100L)
                .email("john@example.com")
                .passwordHash("encoded_secret")
                .firstName("John")
                .roles(Set.of(roleUser))
                .status(User.UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should register new user and assign 14-day trial subscription")
    void testRegisterSuccess() {
        RegisterRequest registerReq = RegisterRequest.builder()
                .email("new@example.com")
                .password("password123")
                .firstName("Jane")
                .build();

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("password123")).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtProvider.generateAccessToken(any(), anyLong())).thenReturn("jwt.access.token");
        when(jwtProvider.generateRefreshToken(anyString())).thenReturn("jwt.refresh.token");
        when(subscriptionService.getSubscriptionForUser(anyLong()))
                .thenReturn(SubscriptionDto.builder().status(SubscriptionStatus.TRIAL).build());

        AuthResponse response = authService.register(registerReq, "127.0.0.1");

        assertNotNull(response);
        assertEquals("jwt.access.token", response.getAccessToken());
        verify(subscriptionService).createTrialSubscription(any(User.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when registering with duplicate email")
    void testRegisterDuplicateEmail() {
        RegisterRequest registerReq = RegisterRequest.builder()
                .email("john@example.com")
                .password("password123")
                .firstName("John")
                .build();

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(registerReq, "127.0.0.1"));
    }

    @Test
    @DisplayName("Should login successfully with correct credentials")
    void testLoginSuccess() {
        LoginRequest loginReq = LoginRequest.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmailWithRolesAndSubscriptions("john@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded_secret")).thenReturn(true);
        when(jwtProvider.generateAccessToken(any(), anyLong())).thenReturn("jwt.access.token");
        when(jwtProvider.generateRefreshToken(anyString())).thenReturn("jwt.refresh.token");
        when(subscriptionService.getSubscriptionForUser(100L))
                .thenReturn(SubscriptionDto.builder().status(SubscriptionStatus.ACTIVE).build());

        AuthResponse response = authService.login(loginReq, "127.0.0.1");

        assertNotNull(response);
        assertEquals("john@example.com", response.getEmail());
    }
}
