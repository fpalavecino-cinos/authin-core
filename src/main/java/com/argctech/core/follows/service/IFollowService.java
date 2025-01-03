package com.argctech.core.follows.service;

import com.argctech.core.follows.dto.FollowDTO;
import com.argctech.core.users.dto.UserDTO;
import com.argctech.core.users.utils.exceptions.UserFollowingException;
import com.argctech.core.users.utils.exceptions.UserNotFoundException;

import java.util.List;

public interface IFollowService {
    FollowDTO followUser(Long fromUserId, Long toUserId) throws UserFollowingException, UserNotFoundException;
    List<UserDTO> getFollowers(Long id) throws UserNotFoundException;
    List<UserDTO> getFollowings(Long id) throws UserNotFoundException;
}
