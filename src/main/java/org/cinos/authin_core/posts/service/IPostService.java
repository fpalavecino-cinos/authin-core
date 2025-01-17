package org.cinos.authin_core.posts.service;

import org.cinos.authin_core.posts.controller.request.PostCreateRequest;
import org.cinos.authin_core.posts.dto.PostDTO;
import org.cinos.authin_core.posts.dto.PostFeedDTO;
import org.cinos.authin_core.posts.dto.PostProfileDTO;
import org.cinos.authin_core.posts.entity.PostEntity;
import org.cinos.authin_core.posts.utils.exceptions.PostNotFoundException;
import org.cinos.authin_core.users.utils.exceptions.UserNotFoundException;
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
    PostDTO createPost(PostCreateRequest request, List<MultipartFile> files) throws IOException, UserNotFoundException;
    List<PostProfileDTO> getPostsProfile(Long userId) throws UserNotFoundException;
    PostEntity getPostEntityById(Long id) throws PostNotFoundException;
    List<PostProfileDTO> getSavedPostsProfile(Long userId) throws UserNotFoundException;
    void saveUserPost(Long userId, Long postId) throws PostNotFoundException, UserNotFoundException;
    Boolean userSavedPost(Long userId, Long postId) throws PostNotFoundException, UserNotFoundException;
    void userUnsavePost(Long userId, Long postId) throws PostNotFoundException;
}
