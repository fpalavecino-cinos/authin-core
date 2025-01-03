package com.argctech.core.users.service;

import com.argctech.core.users.dto.AccountDTO;
import com.argctech.core.users.entity.UserEntity;
import com.argctech.core.users.utils.exceptions.UserNotFoundException;

public interface IAccountService {
    void createUserAccount(UserEntity user) throws UserNotFoundException;
    AccountDTO getUserAccount(Long id) throws UserNotFoundException;
    void incrementFollowings(Long fromUserId);
    void incrementFollowers(Long toUserId);

    AccountDTO getUserLoggedAccount();
}
