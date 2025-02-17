package org.cinos.authin_core.posts.controller;

import org.cinos.authin_core.posts.controller.request.PostCreateRequest;
import org.cinos.authin_core.posts.dto.PostDTO;
import org.cinos.authin_core.posts.dto.PostFeedDTO;
import org.cinos.authin_core.posts.dto.PostProfileDTO;
import org.cinos.authin_core.posts.service.IPostService;
import org.cinos.authin_core.posts.utils.exceptions.PostNotFoundException;
import org.cinos.authin_core.users.utils.exceptions.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);
    private final IPostService postService;

    @GetMapping("/pageable")
    public ResponseEntity<List<PostDTO>> getPostPageable(@RequestParam final Integer page, @RequestParam final Integer size) {
        return ResponseEntity.ok(postService.getPostPageable(page, size));
    }

    @GetMapping("/feed/{userId}")
    public ResponseEntity<Page<PostFeedDTO>> getFeedPosts(@PathVariable final Long userId, @RequestParam final Integer page, @RequestParam final Integer size) throws UserNotFoundException {
        ResponseEntity<Page<PostFeedDTO>> result = null;
        try {
            result = ResponseEntity.ok(postService.getFeedPosts(userId, PageRequest.of(page, size)));
        } catch (UserNotFoundException e) {
            log.info(e.getMessage());
        }
        return result;
    }

    @GetMapping("/followings/{userId}")
    public ResponseEntity<Page<PostDTO>> getFollowingsPosts(@PathVariable final Long userId, @RequestParam final Integer page, @RequestParam final Integer size) throws UserNotFoundException {
        return ResponseEntity.ok(postService.getFollowingsPosts(userId, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable final Long id) throws PostNotFoundException {
        return ResponseEntity.ok(postService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostDTO>> getPostsByUserId(@PathVariable final Long userId) {
        return ResponseEntity.ok(postService.getByUserId(userId));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<List<PostProfileDTO>> getPostsProfileByUserId(@PathVariable final Long userId) throws UserNotFoundException {
        return ResponseEntity.ok(postService.getPostsProfile(userId));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/profile/{userId}/saved-posts")
    public ResponseEntity<List<PostProfileDTO>> getSavedPostsProfile(@PathVariable final Long userId) throws UserNotFoundException {
        return ResponseEntity.ok(postService.getSavedPostsProfile(userId));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, value = "/create")
    public ResponseEntity<PostDTO> createPost(
            @RequestParam("post") final String request,
            @RequestParam("images") final List<MultipartFile> images) throws IOException, UserNotFoundException {
        ObjectMapper objectMapper = new ObjectMapper();
        PostCreateRequest post = objectMapper.readValue(request, PostCreateRequest.class);
        return ResponseEntity.ok(postService.createPost(post, images));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/account/{userId}/save-post/{postId}")
    public ResponseEntity<Object> saveUserPost(@PathVariable final Long userId, @PathVariable final Long postId) throws UserNotFoundException, PostNotFoundException {
        postService.saveUserPost(userId, postId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/account/{userId}/unsave-post/{postId}")
    public ResponseEntity<Object> userUnsavePost(@PathVariable final Long userId, @PathVariable final Long postId) throws PostNotFoundException {
        postService.userUnsavePost(userId, postId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/account/{userId}/saved/{postId}")
    public ResponseEntity<Boolean> userSavedPost(@PathVariable final Long userId, @PathVariable final Long postId) throws UserNotFoundException, PostNotFoundException {
        return ResponseEntity.ok(postService.userSavedPost(userId, postId));
    }


}
