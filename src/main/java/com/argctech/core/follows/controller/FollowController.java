package com.argctech.core.follows.controller;

import com.argctech.core.follows.dto.FollowDTO;
import com.argctech.core.follows.service.FollowService;
import com.argctech.core.users.dto.UserDTO;
import com.argctech.core.users.utils.exceptions.UserFollowingException;
import com.argctech.core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping
    public ResponseEntity<FollowDTO> followUser(@RequestParam final Long fromUserId, @RequestParam final Long toUserId) throws UserFollowingException, UserNotFoundException {
        return ResponseEntity.ok(followService.followUser(fromUserId, toUserId));
    }

    @GetMapping("/followers/{id}")
    public ResponseEntity<List<UserDTO>> getFollowers(@PathVariable final Long id) throws UserNotFoundException {
        return ResponseEntity.ok(followService.getFollowers(id));
    }

    @GetMapping("/followings/{id}")
    public ResponseEntity<List<UserDTO>> getFollowings(@PathVariable final Long id) throws UserNotFoundException {
        return ResponseEntity.ok(followService.getFollowings(id));
    }

}
