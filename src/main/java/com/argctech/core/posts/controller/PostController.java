package com.argctech.core.posts.controller;

import com.argctech.core.posts.controller.request.PostCreateRequest;
import com.argctech.core.posts.dto.PostDTO;
import com.argctech.core.posts.dto.PostFeedDTO;
import com.argctech.core.posts.service.IPostService;
import com.argctech.core.posts.utils.exceptions.PostNotFoundException;
import com.argctech.core.users.utils.exceptions.UserNotFoundException;
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

    @PreAuthorize("hasRole('USER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, value = "/create")
    public ResponseEntity<PostDTO> createPost(
            @RequestParam("post") final String request,
            @RequestParam("images") final List<MultipartFile> images) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        PostCreateRequest post = objectMapper.readValue(request, PostCreateRequest.class);
        return ResponseEntity.ok(postService.createPost(post, images));
    }

}
