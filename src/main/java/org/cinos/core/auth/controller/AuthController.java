package org.cinos.core.auth.controller;

import org.cinos.core.auth.controller.request.LoginRequest;
import org.cinos.core.auth.controller.response.LoginResponse;
import org.cinos.core.auth.controller.response.RegisterResponse;
import org.cinos.core.auth.service.AuthService;
import org.cinos.core.users.controller.request.UserCreateRequest;
import org.cinos.core.users.utils.exceptions.DuplicateUserException;
import org.cinos.core.users.utils.exceptions.PasswordDontMatchException;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid final UserCreateRequest userCreateRequest) throws PasswordDontMatchException, DuplicateUserException {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(userCreateRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid final LoginRequest userCreateRequest) throws UserNotFoundException {
        return ResponseEntity.ok(authService.login(userCreateRequest));
    }

    @GetMapping("/refresh")
    public ResponseEntity<String> refresh(@RequestParam String refreshToken) throws UserNotFoundException {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

}
