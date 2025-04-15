package org.cinos.authin_core.users.service;

import org.cinos.authin_core.users.controller.request.UserCreateRequest;
import org.cinos.authin_core.users.dto.UserDTO;
import org.cinos.authin_core.users.entity.UserEntity;
import org.cinos.authin_core.users.utils.exceptions.DuplicateUserException;
import org.cinos.authin_core.users.utils.exceptions.PasswordDontMatchException;
import org.cinos.authin_core.users.utils.exceptions.UserNotFoundException;

import java.util.List;

public interface IUserService {
    List<UserDTO> getUsers();
    String getFullName(Long id) throws UserNotFoundException;
    List<UserDTO> getUsersPageable(Integer page, Integer size);
    UserDTO getUserById(Long id) throws UserNotFoundException;
    UserDTO createUser(UserCreateRequest user) throws PasswordDontMatchException, DuplicateUserException;
    UserDTO getByUsername(String username) throws UserNotFoundException;
    UserEntity getByUsernameEntity(String username) throws UserNotFoundException;
    UserEntity getByIdEntity(Long id) throws UserNotFoundException;
    UserDTO getLoggedUser();
}
