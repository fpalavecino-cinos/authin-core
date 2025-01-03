package com.argctech.core.users.service;

import com.argctech.core.users.controller.request.UserCreateRequest;
import com.argctech.core.follows.dto.FollowDTO;
import com.argctech.core.users.dto.AccountDTO;
import com.argctech.core.users.dto.UserDTO;
import com.argctech.core.users.entity.UserEntity;
import com.argctech.core.users.utils.exceptions.PasswordDontMatchException;
import com.argctech.core.users.utils.exceptions.UserFollowingException;
import com.argctech.core.users.utils.exceptions.UserNotFoundException;

import java.util.List;

public interface IUserService {
    List<UserDTO> getUsers();
    String getFullName(Long id) throws UserNotFoundException;
    List<UserDTO> getUsersPageable(Integer page, Integer size);
    UserDTO getUserById(Long id) throws UserNotFoundException;
    UserDTO createUser(UserCreateRequest user) throws PasswordDontMatchException;
    UserDTO getByUsername(String username) throws UserNotFoundException;
    UserEntity getByUsernameEntity(String username) throws UserNotFoundException;
    UserEntity getByIdEntity(Long id) throws UserNotFoundException;
    UserDTO getLoggedUser();
}
