package com.orderflow.controller;

import com.orderflow.dto.ApiResponse;
import com.orderflow.dto.auth.UpdatePasswordRequest;
import com.orderflow.dto.auth.UpdateProfileRequest;
import com.orderflow.dto.auth.UserProfileDto;
import com.orderflow.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Profile", description = "User profile and password management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final AuthService authService;

    @GetMapping
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(authService.getProfile(authentication.getName())));
    }

    @PutMapping
    @Operation(summary = "Update user profile details")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.updateProfile(authentication.getName(), request), "Profile updated successfully"));
    }

    @PutMapping("/password")
    @Operation(summary = "Update account password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            Authentication authentication,
            @Valid @RequestBody UpdatePasswordRequest request) {
        authService.updatePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password updated successfully"));
    }
}
