package org.cinos.authin_core.users.service;

import org.cinos.authin_core.users.dto.AccountDTO;
import org.cinos.authin_core.users.entity.UserEntity;
import org.cinos.authin_core.users.utils.exceptions.UserNotFoundException;

public interface IAccountService {
    void createUserAccount(UserEntity user) throws UserNotFoundException;
    AccountDTO getUserAccount(Long id) throws UserNotFoundException;
    void incrementFollowings(Long fromUserId);
    void incrementFollowers(Long toUserId);

    AccountDTO getUserLoggedAccount();
}
