package com.argctech.core.auth.controller;

import com.argctech.core.auth.controller.request.LoginRequest;
import com.argctech.core.auth.controller.response.LoginResponse;
import com.argctech.core.auth.controller.response.RegisterResponse;
import com.argctech.core.auth.service.AuthService;
import com.argctech.core.users.controller.request.UserCreateRequest;
import com.argctech.core.users.dto.UserDTO;
import com.argctech.core.users.utils.exceptions.PasswordDontMatchException;
import com.argctech.core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody final UserCreateRequest userCreateRequest) throws PasswordDontMatchException {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(userCreateRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> register(@RequestBody final LoginRequest userCreateRequest) throws UserNotFoundException {
        return ResponseEntity.ok(authService.login(userCreateRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@RequestBody String refreshToken) throws UserNotFoundException {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

}
