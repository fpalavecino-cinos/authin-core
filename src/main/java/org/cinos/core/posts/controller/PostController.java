package org.cinos.core.posts.controller;

import org.cinos.core.posts.controller.request.PostCreateRequest;
import org.cinos.core.posts.dto.*;
import org.cinos.core.posts.service.ICommentService;
import org.cinos.core.posts.service.IMakeService;
import org.cinos.core.posts.service.IModelService;
import org.cinos.core.posts.service.IPostService;
import org.cinos.core.posts.utils.exceptions.PostNotFoundException;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;
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
    private final IMakeService makeService;
    private final IModelService modelService;
    private final ICommentService commentService;

    @GetMapping("/pageable")
    public ResponseEntity<List<PostDTO>> getPostPageable(@RequestParam final Integer page, @RequestParam final Integer size) {
        return ResponseEntity.ok(postService.getPostPageable(page, size));
    }

    @PostMapping("/filter")
    public ResponseEntity<Page<PostDTO>> getPostsFilter(@RequestBody final PostFilterDTO postFilterDTO) {
        return ResponseEntity.ok(postService.getPostsFilter(postFilterDTO));
    }

    @GetMapping("/feed/{userId}")
    public ResponseEntity<Page<PostFeedDTO>> getFeedPosts(@PathVariable final Long userId,
                                                          @RequestParam final Integer page,
                                                          @RequestParam final Integer size,
                                                          @RequestParam final Double latitude,
                                                          @RequestParam final Double longitude) {
        ResponseEntity<Page<PostFeedDTO>> result = null;
        try {
            result = ResponseEntity.ok(postService.getFeedPosts(userId, PageRequest.of(page, size), latitude, longitude));
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

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/makes")
    public ResponseEntity<List<MakeDTO>> getMakes(@RequestParam final String q) {
        return ResponseEntity.ok(makeService.findByNameContainingIgnoreCase(q));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/models")
    public ResponseEntity<List<ModelDTO>> getModels(@RequestParam final String make) {
        return ResponseEntity.ok(modelService.findAllByMakeName(make));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}/comments")
    public ResponseEntity<Page<CommentDTO>> getComments(@PathVariable final Long id, @RequestParam final Integer page, @RequestParam final Integer size) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(id, PageRequest.of(page, size)));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}/comments-length")
    public ResponseEntity<Integer> getCommentsLength(@PathVariable final Long id) {
        return ResponseEntity.ok(commentService.getCommentsLength(id));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/comment")
    public ResponseEntity<CommentDTO> sendComment(@RequestBody final CommentDTO commentDTO) throws UserNotFoundException {
        return ResponseEntity.ok(commentService.createComment(commentDTO));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/deactivate/{id}")
    public ResponseEntity<Object> deactivatePost(@PathVariable Long id) throws PostNotFoundException {
        postService.deactivatePost(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, value = "/upload-documentation")
    public ResponseEntity<Object> uploadDocumentation(@RequestParam("postId") final Long postId,
                                                      @RequestParam("docs") final List<MultipartFile> docs) throws PostNotFoundException, IOException {
        postService.uploadDocumentation(postId, docs);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/technical-verification-status")
    public ResponseEntity<?> getTechnicalVerificationStatus(@PathVariable Long id) {
        try {
            var post = postService.getPostEntityById(id);
            var technicalVerification = post.getTechnicalVerification();
            if (technicalVerification == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(org.cinos.core.technical_verification.dto.VerificationStatusResponse.builder()
                    .status(technicalVerification.getStatus())
                    .sentToVerificationDate(technicalVerification.getSentToVerificationDate())
                    .verificationAcceptedDate(technicalVerification.getVerificationAcceptedDate())
                    .verificationAppointmentDate(technicalVerification.getVerificationAppointmentDate())
                    .isApproved(technicalVerification.getIsApproved())
                    .verificationMadeDate(technicalVerification.getVerificationMadeDate())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("No se pudo obtener el informe técnico: " + e.getMessage());
        }
    }


}
