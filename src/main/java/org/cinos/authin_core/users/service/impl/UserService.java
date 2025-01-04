package org.cinos.authin_core.users.service.impl;

import org.cinos.authin_core.users.controller.request.UserCreateRequest;
import org.cinos.authin_core.users.dto.DTOConverter;
import org.cinos.authin_core.users.dto.UserDTO;
import org.cinos.authin_core.users.entity.UserEntity;
import org.cinos.authin_core.users.model.Role;
import org.cinos.authin_core.users.repository.UserRepository;
import org.cinos.authin_core.users.service.IUserService;
import org.cinos.authin_core.users.utils.exceptions.PasswordDontMatchException;
import org.cinos.authin_core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final String USER_NOT_FOUND_MESSSAGE = "Usuario no encontrado";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;

    @Override
    public List<UserDTO> getUsers() {
        List<UserEntity> usersDB = userRepository.findAll();
        return usersDB.stream().map(e -> DTOConverter.toDTO(e, UserDTO.class)).toList();
    }

    @Override
    public String getFullName(final Long id) throws UserNotFoundException {
        UserEntity user = userRepository.findById(id).orElseThrow(()->new UserNotFoundException(USER_NOT_FOUND_MESSSAGE));
        return String.format("%s %s", user.getName(), user.getLastname());
    }

    @Override
    public List<UserDTO> getUsersPageable(Integer page, Integer size) {
        Page<UserEntity> usersDB = userRepository.findAll(PageRequest.of(page, size));
        return usersDB.stream().map(e -> DTOConverter.toDTO(e, UserDTO.class)).toList();
    }

    @Override
    public UserDTO getUserById(Long id) throws UserNotFoundException {
        return userRepository.findById(id, UserDTO.class).orElseThrow(()->new UserNotFoundException(USER_NOT_FOUND_MESSSAGE));
    }

    @Override
    public UserDTO createUser(UserCreateRequest request) throws PasswordDontMatchException {
        passwordsMatch(request.password(), request.repeatPassword());

        UserEntity userEntity = UserEntity.builder()
                .name(request.name())
                .lastname(request.lastname())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();
        UserEntity userDB = userRepository.save(userEntity);
        accountService.createUserAccount(userDB);
        return DTOConverter.toDTO(userDB, UserDTO.class);
    }

    @Override
    public UserDTO getByUsername(String username) throws UserNotFoundException {
        return userRepository.findByUsername(username, UserDTO.class).orElseThrow(()->new UserNotFoundException(USER_NOT_FOUND_MESSSAGE));
    }

    @Override
    public UserEntity getByUsernameEntity(String username) throws UserNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(()->new UserNotFoundException(USER_NOT_FOUND_MESSSAGE));
    }

    @Override
    public UserEntity getByIdEntity(Long id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(()->new UserNotFoundException(USER_NOT_FOUND_MESSSAGE));
    }

    @Override
    public UserDTO getLoggedUser(){
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        return DTOConverter.toDTO(userEntity, UserDTO.class);
    }

    private void passwordsMatch(String password, String repeatPassword) throws PasswordDontMatchException {
        if(!StringUtils.hasText(password) || !StringUtils.hasText(repeatPassword)){
            throw new PasswordDontMatchException("Las contraseñas no pueden estar vacias");
        }
        if (!password.equals(repeatPassword)) {
            throw new PasswordDontMatchException("Las contraseñas no coinciden");
        }
    }

}
