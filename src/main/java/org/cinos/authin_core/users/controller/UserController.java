package org.cinos.authin_core.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cinos.authin_core.posts.controller.request.PostCreateRequest;
import org.cinos.authin_core.users.controller.request.UserCreateRequest;
import org.cinos.authin_core.users.dto.AccountDTO;
import org.cinos.authin_core.users.dto.UpdateAccountDTO;
import org.cinos.authin_core.users.dto.UserDTO;
import org.cinos.authin_core.users.service.IAccountService;
import org.cinos.authin_core.users.service.IUserService;
import org.cinos.authin_core.users.utils.exceptions.DuplicateUserException;
import org.cinos.authin_core.users.utils.exceptions.PasswordDontMatchException;
import org.cinos.authin_core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
    public ResponseEntity<UserDTO> createUser(@RequestBody final UserCreateRequest userRequest) throws PasswordDontMatchException, DuplicateUserException {
        return ResponseEntity.ok(userService.createUser(userRequest));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/logged")
    public ResponseEntity<UserDTO> getUserLogged() {
        return ResponseEntity.ok(userService.getLoggedUser());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/account/logged")
    public ResponseEntity<AccountDTO> getUserLoggedAccount() {
        return ResponseEntity.ok(accountService.getUserLoggedAccount());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/account/{id}")
    public ResponseEntity<AccountDTO> getUserAccount(@PathVariable final Long id) throws UserNotFoundException {
        return ResponseEntity.ok(accountService.getUserAccount(id));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, value = "/account/update")
    public ResponseEntity<Void> updateUserAccount(
            @RequestParam("account") String account,
            @RequestParam("image") MultipartFile image) throws UserNotFoundException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        UpdateAccountDTO accountDTO = objectMapper.readValue(account, UpdateAccountDTO.class);
        accountService.updateUserAccount(accountDTO, image);
        return ResponseEntity.ok().build();
    }

}
