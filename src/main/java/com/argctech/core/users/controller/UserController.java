package com.argctech.core.users.controller;

import com.argctech.core.users.controller.request.UserCreateRequest;
import com.argctech.core.users.dto.AccountDTO;
import com.argctech.core.users.dto.UserDTO;
import com.argctech.core.users.service.IAccountService;
import com.argctech.core.users.service.IUserService;
import com.argctech.core.users.utils.exceptions.PasswordDontMatchException;
import com.argctech.core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final IAccountService accountService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable final Long id) throws UserNotFoundException {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/fullname/{id}")
    public ResponseEntity<String> getFullName(@PathVariable final Long id) throws UserNotFoundException {
        return ResponseEntity.ok(userService.getFullName(id));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<UserDTO> createUser(@RequestBody final UserCreateRequest userRequest) throws PasswordDontMatchException {
        return ResponseEntity.ok(userService.createUser(userRequest));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/logged")
    public ResponseEntity<UserDTO> getUserLogged() {
        return ResponseEntity.ok(userService.getLoggedUser());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/logged/account")
    public ResponseEntity<AccountDTO> getUserLoggedAccount() {
        return ResponseEntity.ok(accountService.getUserLoggedAccount());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/account/{id}")
    public ResponseEntity<AccountDTO> getUserAccount(@PathVariable final Long id) throws UserNotFoundException {
        return ResponseEntity.ok(accountService.getUserAccount(id));
    }

}
