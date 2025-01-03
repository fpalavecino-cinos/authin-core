package com.argctech.core.posts.service;

import com.argctech.core.posts.controller.request.PostCreateRequest;
import com.argctech.core.posts.dto.PostDTO;
import com.argctech.core.posts.dto.PostFeedDTO;
import com.argctech.core.posts.utils.exceptions.PostNotFoundException;
import com.argctech.core.users.utils.exceptions.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IPostService {
    List<PostDTO> getPostPageable(Integer page, Integer size);
    Page<PostFeedDTO> getFeedPosts(Long userId, Pageable pageable) throws UserNotFoundException;
    Page<PostDTO> getFollowingsPosts(Long userId, Pageable pageable) throws UserNotFoundException;
    PostDTO getById(Long id) throws PostNotFoundException;
    List<PostDTO> getByUserId(Long userId);
    PostDTO createPost(PostCreateRequest request, List<MultipartFile> files) throws IOException;

}
