package org.cinos.authin_core.users.service;

import org.cinos.authin_core.posts.utils.exceptions.PostNotFoundException;
import org.cinos.authin_core.users.dto.AccountDTO;
import org.cinos.authin_core.users.dto.UpdateAccountDTO;
import org.cinos.authin_core.users.entity.AccountEntity;
import org.cinos.authin_core.users.entity.UserEntity;
import org.cinos.authin_core.users.utils.exceptions.UserNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IAccountService {
    void createUserAccount(UserEntity user) throws UserNotFoundException;
    AccountDTO getUserAccount(Long id) throws UserNotFoundException;
    void incrementFollowings(Long fromUserId);
    void incrementFollowers(Long toUserId);
    AccountDTO getUserLoggedAccount();
    AccountEntity getAccountEntityById(Long id) throws UserNotFoundException;

    void decrementFollowings(Long fromUserId);
    void decrementFollowers(Long fromUserId);
    List<AccountEntity> findByUsernameContainingIgnoreCase(String query);
    void updateUserAccount(UpdateAccountDTO accountDTO, MultipartFile file) throws UserNotFoundException, IOException;
}
