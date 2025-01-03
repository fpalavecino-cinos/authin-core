package com.argctech.core.auth.service;

import com.argctech.core.auth.controller.request.LoginRequest;
import com.argctech.core.auth.controller.response.LoginResponse;
import com.argctech.core.auth.controller.response.RegisterResponse;
import com.argctech.core.users.controller.request.UserCreateRequest;
import com.argctech.core.users.dto.DTOConverter;
import com.argctech.core.users.dto.UserDTO;
import com.argctech.core.users.entity.UserEntity;
import com.argctech.core.users.service.impl.UserService;
import com.argctech.core.users.utils.exceptions.PasswordDontMatchException;
import com.argctech.core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public RegisterResponse register(final UserCreateRequest userCreateRequest) throws PasswordDontMatchException {
        final UserDTO userDTO = userService.createUser(userCreateRequest);
        final String token = jwtService.generateToken(userDTO);
        return RegisterResponse.builder()
                .id(userDTO.id())
                .username(userDTO.username())
                .email(userDTO.email())
                .name(userDTO.name())
                .role(userDTO.role().name())
                .token(token)
                .build();
    }

    public LoginResponse login(final LoginRequest loginRequest) throws UserNotFoundException {
        Authentication authentication = new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());
        authenticationManager.authenticate(authentication);

        UserDTO user = userService.getByUsername(loginRequest.username());
        String jwt = jwtService.generateToken(user);

        return LoginResponse.builder()
                .name(user.name())
                .lastname(user.lastname())
                .username(user.username())
                .email(user.email())
                .role(user.role().name())
                .token(jwt)
                .build();
    }

}
