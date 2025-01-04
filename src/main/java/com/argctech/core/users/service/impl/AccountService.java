package com.argctech.core.users.service.impl;

import com.argctech.core.users.dto.AccountDTO;
import com.argctech.core.users.dto.DTOConverter;
import com.argctech.core.users.dto.UserAccountDTO;
import com.argctech.core.users.dto.UserDTO;
import com.argctech.core.users.entity.AccountEntity;
import com.argctech.core.users.entity.UserEntity;
import com.argctech.core.users.repository.AccountRepository;
import com.argctech.core.users.service.IAccountService;
import com.argctech.core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {

    private final AccountRepository accountRepository;

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

}
