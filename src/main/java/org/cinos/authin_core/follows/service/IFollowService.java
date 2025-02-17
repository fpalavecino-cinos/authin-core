package org.cinos.authin_core.follows.service;

import org.cinos.authin_core.follows.dto.FollowDTO;
import org.cinos.authin_core.users.dto.UserDTO;
import org.cinos.authin_core.users.utils.exceptions.UserFollowingException;
import org.cinos.authin_core.users.utils.exceptions.UserNotFoundException;

import java.util.List;

public interface IFollowService {
    FollowDTO followUser(Long fromUserId, Long toUserId) throws UserFollowingException, UserNotFoundException;
    List<UserDTO> getFollowers(Long id) throws UserNotFoundException;
    List<UserDTO> getFollowings(Long id) throws UserNotFoundException;

    Boolean isFollowing(Long fromUserId, Long toUserId);

    FollowDTO unfollowUser(Long fromUserId, Long toUserId);
}
