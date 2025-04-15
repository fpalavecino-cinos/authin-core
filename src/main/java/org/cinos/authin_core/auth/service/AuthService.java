package org.cinos.authin_core.auth.service;

import org.cinos.authin_core.auth.controller.request.LoginRequest;
import org.cinos.authin_core.auth.controller.response.LoginResponse;
import org.cinos.authin_core.auth.controller.response.RegisterResponse;
import org.cinos.authin_core.users.controller.request.UserCreateRequest;
import org.cinos.authin_core.users.dto.UserDTO;
import org.cinos.authin_core.users.service.impl.UserService;
import org.cinos.authin_core.users.utils.exceptions.DuplicateUserException;
import org.cinos.authin_core.users.utils.exceptions.PasswordDontMatchException;
import org.cinos.authin_core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public RegisterResponse register(final UserCreateRequest userCreateRequest) throws PasswordDontMatchException, DuplicateUserException {
        final UserDTO userDTO = userService.createUser(userCreateRequest);
        final String accessToken = jwtService.generateToken(userDTO);
        final String refreshToken = jwtService.generateRefreshToken(userDTO);
        return RegisterResponse.builder()
                .id(userDTO.id())
                .username(userDTO.username())
                .email(userDTO.email())
                .name(userDTO.name())
                .role(userDTO.role().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public LoginResponse login(final LoginRequest loginRequest) throws UserNotFoundException {
        final Authentication authentication = new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());
        authenticationManager.authenticate(authentication);

        final UserDTO user = userService.getByUsername(loginRequest.username());
        final String accessToken = jwtService.generateToken(user);
        final String refreshToken = jwtService.generateRefreshToken(user);

        return LoginResponse.builder()
                .name(user.name())
                .lastname(user.lastname())
                .username(user.username())
                .email(user.email())
                .role(user.role().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String refreshToken(String refreshToken) throws UserNotFoundException {
        if (!jwtService.isValidRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh token inválido o expirado");
        }
        String username = jwtService.extractUsername(refreshToken);
        UserDTO user = userService.getByUsername(username);

        return jwtService.generateToken(user);
    }
}
