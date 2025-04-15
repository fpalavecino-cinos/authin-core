package org.cinos.authin_core.users.service.impl;

import org.cinos.authin_core.posts.entity.PostEntity;
import org.cinos.authin_core.posts.service.impl.PostService;
import org.cinos.authin_core.posts.service.impl.StorageService;
import org.cinos.authin_core.posts.utils.exceptions.PostNotFoundException;
import org.cinos.authin_core.users.dto.AccountDTO;
import org.cinos.authin_core.users.dto.DTOConverter;
import org.cinos.authin_core.users.dto.UpdateAccountDTO;
import org.cinos.authin_core.users.entity.AccountEntity;
import org.cinos.authin_core.users.entity.UserEntity;
import org.cinos.authin_core.users.repository.AccountRepository;
import org.cinos.authin_core.users.service.IAccountService;
import org.cinos.authin_core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {

    private final AccountRepository accountRepository;
    private final StorageService storageService;

    @Override
    public void createUserAccount(final UserEntity user) {
        AccountEntity account = AccountEntity.builder()
                .user(user)
                .avatarImg("https://ionicframework.com/docs/img/demos/avatar.svg")
                .points(0)
                .followers(0L)
                .followings(0L)
                .posts(0)
                .build();

        accountRepository.save(account);
    }

    @Override
    public AccountDTO getUserAccount(Long id) throws UserNotFoundException {
        AccountEntity entity = accountRepository.findById(id).orElseThrow(()->new UserNotFoundException("Usuario no encontrado"));
        return AccountDTO.builder()
                .id(entity.getId())
                .email(entity.getUser().getEmail())
                .name(entity.getUser().getName())
                .lastname(entity.getUser().getLastname())
                .username(entity.getUser().getUsername())
                .points(entity.getPoints())
                .followers(entity.getFollowers())
                .followings(entity.getFollowings())
                .avatarImg(entity.getAvatarImg())
                .build();
    }

    @Override
    public void incrementFollowings(Long fromUserId) {
        AccountEntity account = accountRepository.findById(fromUserId).orElseThrow(()->new UsernameNotFoundException("Usuario no encontrado"));
        account.setFollowings(account.getFollowings() + 1);
        accountRepository.save(account);
    }

    @Override
    public void incrementFollowers(Long toUserId) {
        AccountEntity account = accountRepository.findById(toUserId).orElseThrow(()->new UsernameNotFoundException("Usuario no encontrado"));
        account.setFollowers(account.getFollowers() + 1);
        accountRepository.save(account);
    }

    @Override
    public AccountDTO getUserLoggedAccount() {
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        AccountEntity accountEntity = accountRepository.findById(userEntity.getId()).orElseThrow(()->new UsernameNotFoundException("Usuario no encontrado"));
        return AccountDTO.builder()
                .id(accountEntity.getId())
                .email(accountEntity.getUser().getEmail())
                .name(accountEntity.getUser().getName())
                .lastname(accountEntity.getUser().getLastname())
                .username(accountEntity.getUser().getUsername())
                .points(accountEntity.getPoints())
                .followers(accountEntity.getFollowers())
                .followings(accountEntity.getFollowings())
                .avatarImg(accountEntity.getAvatarImg())
                .build();
    }

    @Override
    public AccountEntity getAccountEntityById(Long id) throws UserNotFoundException {
        return accountRepository.findByUser_Id(id).orElseThrow(()->new UserNotFoundException("Usuario no encontrado"));
    }

    @Override
    public void decrementFollowings(Long fromUserId) {
        AccountEntity account = accountRepository.findById(fromUserId).orElseThrow(()->new UsernameNotFoundException("Usuario no encontrado"));
        account.setFollowings(account.getFollowings() - 1);
        accountRepository.save(account);
    }

    @Override
    public void decrementFollowers(Long toUserId) {
        AccountEntity account = accountRepository.findById(toUserId).orElseThrow(()->new UsernameNotFoundException("Usuario no encontrado"));
        account.setFollowers(account.getFollowers() - 1);
        accountRepository.save(account);
    }

    @Override
    public List<AccountEntity> findByUsernameContainingIgnoreCase(String username) {
        return accountRepository.findByUser_UsernameContainingIgnoreCase(username);
    }

    @Override
    public void updateUserAccount(final UpdateAccountDTO accountDTO, MultipartFile file) throws IOException {
        AccountEntity accountEntity = accountRepository.findById(accountDTO.id()).orElseThrow(()->new UsernameNotFoundException("Usuario no encontrado"));
        String avatarImgUrl = storageService.uploadFile(file);
        accountEntity.getUser().setName(accountDTO.name());
        accountEntity.getUser().setLastname(accountDTO.lastname());
        accountEntity.setAvatarImg(avatarImgUrl);
        accountRepository.save(accountEntity);
    }

}
