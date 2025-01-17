package org.cinos.authin_core.posts.service.impl;

import org.cinos.authin_core.posts.controller.request.PostCreateRequest;
import org.cinos.authin_core.posts.dto.PostDTO;
import org.cinos.authin_core.posts.dto.PostFeedDTO;
import org.cinos.authin_core.posts.dto.PostProfileDTO;
import org.cinos.authin_core.posts.dto.mapper.PostMapper;
import org.cinos.authin_core.posts.entity.PostEntity;
import org.cinos.authin_core.posts.entity.PostImageEntity;
import org.cinos.authin_core.posts.entity.PostLocationEntity;
import org.cinos.authin_core.posts.repository.PostImageRepository;
import org.cinos.authin_core.posts.repository.PostLocationRepository;
import org.cinos.authin_core.posts.repository.PostRepository;
import org.cinos.authin_core.posts.repository.specs.PostSpecifications;
import org.cinos.authin_core.posts.service.IPostService;
import org.cinos.authin_core.posts.utils.exceptions.PostNotFoundException;
import org.cinos.authin_core.follows.service.IFollowService;
import org.cinos.authin_core.users.dto.UserDTO;
import org.cinos.authin_core.users.service.impl.AccountService;
import org.cinos.authin_core.users.service.impl.UserService;
import org.cinos.authin_core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService implements IPostService {

    private final PostRepository postRepository;
    private final IFollowService followService;
//  private final KafkaTemplate<String, String> kafkaTemplate;
    private final String POST_NOT_FOUND = "La publicacion no se encontró";
    private final UserService userService;
    private final StorageService storageService;
    private final PostImageRepository postImageRepository;
    private final PostLocationRepository postLocationRepository;
    private final AccountService accountService;
    private final PostMapper postMapper;

    @Override
    public List<PostDTO> getPostPageable(Integer page, Integer size) {
        List<PostEntity> entityList = postRepository.findAll(PageRequest.of(page, size)).toList();
        return entityList.stream().map(postMapper::toDTO).toList();
    }

    @Override
    public Page<PostFeedDTO> getFeedPosts(Long userId, Pageable pageable) throws UserNotFoundException {
        List<UserDTO> followings = followService.getFollowings(userId);
        List<Long> followingsIds = followings.stream().map(UserDTO::id).toList();
        Specification<PostEntity> spec = PostSpecifications.postsOfFollowingsOrAll(followingsIds,LocalDateTime.now().minusDays(30));

        Page<PostEntity> postEntityPage = postRepository.findAll(spec, pageable);

        return postEntityPage.map((e)-> {
            String userFullName = "";
            try {
                userFullName = userService.getFullName(e.getUserAccount().getId());
            } catch (UserNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            return PostFeedDTO.builder()
                    .id(e.getId())
                    .model(e.getModel())
                    .price(e.getPrice())
                    .year(e.getYear())
                    .make(e.getMake())
                    .isUsed(e.getIsUsed())
                    .userFullName(userFullName)
                    .dateTimeValue(getPostDateTimeValue(e.getPublicationDate()))
                    .imagesUrls(e.getImages().stream().map(PostImageEntity::getUrl).toList())
                    .currencySymbol(e.getCurrencySymbol())
                    .build();
        });

    }

    public String getPostDateTimeValue(LocalDateTime publicationDate){
        LocalDateTime today = LocalDateTime.now();
        long years = Math.abs(ChronoUnit.YEARS.between(publicationDate, today));
        long months = Math.abs(ChronoUnit.MONTHS.between(publicationDate, today));
        long days = Math.abs(ChronoUnit.DAYS.between(publicationDate, today));
        System.out.println(days);
        long hours = Math.abs(ChronoUnit.HOURS.between(publicationDate, today));
        System.out.println(hours);
        long minutes = Math.abs(ChronoUnit.MINUTES.between(publicationDate, today));
        long seconds = Math.abs(ChronoUnit.SECONDS.between(publicationDate, today));
        if (days>=7 && days<14){
            return "1 smn";
        } else if (days>=14 && days<21){
            return "2 smn";
        } else if (days>=21 && days<28){
            return "3 smn";
        } else if (days > 0) {
            return days + "d";
        } else if (hours > 0) {
            return hours + "h";
        } else if (minutes > 0) {
            return minutes + "mn";
        } else if (seconds > 0){
            return seconds + "s";
        } else if (years > 0){
            return years + "a";
        } else if (months > 0) {
            return months + "m";
        } else {
            return "now";
        }
    }

    @Override
    public Page<PostDTO> getFollowingsPosts(Long userId, Pageable pageable) throws UserNotFoundException {
        List<UserDTO> followings = followService.getFollowings(userId);
        List<Long> followingsIds = followings.stream().map(UserDTO::id).toList();
        return postRepository.findAllByUserAccount_IdInOrderByPublicationDateDesc(followingsIds, pageable).map(postMapper::toDTO);
    }

    @Override
    public PostDTO getById(Long id) throws PostNotFoundException {
        return postMapper.toDTO(postRepository.findById(id).orElseThrow(()->new PostNotFoundException(POST_NOT_FOUND)));
    }

    @Override
    public List<PostDTO> getByUserId(Long userId) {
        return postRepository.findByUserAccount_Id(userId).stream().map(postMapper::toDTO).toList();
    }

    @Override
    public PostDTO createPost(PostCreateRequest request, List<MultipartFile> images) throws IOException, UserNotFoundException {
        PostEntity postEntity = PostEntity.builder()
                .make(request.make())
                .model(request.model())
                .kilometers(request.kilometers())
                .fuel(request.fuel())
                .transmission(request.transmission())
                .year(request.year())
                .isUsed(request.isUsed())
                .price(request.price())
                .description(request.description())
                .userAccount(accountService.getAccountEntityById(request.userId()))
                .publicationDate(LocalDateTime.now())
                .active(request.active())
                .currencySymbol(request.currencySymbol())
                .build();

        List<String> imageUrls = storageService.uploadFiles(images);
        List<PostImageEntity> imagesEntity = imageUrls.stream().map(url -> PostImageEntity.builder()
                .url(url)
                .post(postEntity)
                .build()).toList();

        PostLocationEntity location = PostLocationEntity.builder()
                .lat(request.location().lat())
                .lng(request.location().lng())
                .post(postEntity)
                .build();

        postEntity.setImages(imagesEntity);
        postEntity.setLocation(location);
        postRepository.save(postEntity);
        postImageRepository.saveAll(imagesEntity);
        postLocationRepository.save(location);
        return postMapper.toDTO(postEntity);
    }

    @Override
    public List<PostProfileDTO> getPostsProfile(Long userId) throws UserNotFoundException {
        List<PostEntity> posts = postRepository.findAllByUserAccount_Id(userId);
        return posts.stream().map(e -> PostProfileDTO.builder()
                .id(e.getId())
                .firstImage(e.getImages().get(0).getUrl())
                .build()).toList();
    }

    @Override
    public PostEntity getPostEntityById(Long id) throws PostNotFoundException {
        return postRepository.findById(id).orElseThrow(()->new PostNotFoundException(POST_NOT_FOUND));
    }

    @Override
    public List<PostProfileDTO> getSavedPostsProfile(final Long userId) throws UserNotFoundException {
        List<PostEntity> posts = postRepository.findByUsersSaved_Id(userId);
        return posts.stream().map(e -> PostProfileDTO.builder()
                .id(e.getId())
                .firstImage(e.getImages().get(0).getUrl())
                .build()).toList();

    }

    @Override
    public void saveUserPost(final Long userId, final Long postId) throws PostNotFoundException, UserNotFoundException {
        PostEntity post = postRepository.findById(postId).orElseThrow(()->new PostNotFoundException(POST_NOT_FOUND));
        post.getUsersSaved().add(accountService.getAccountEntityById(userId));
        postRepository.save(post);
    }

    @Override
    public Boolean userSavedPost(Long userId, Long postId) throws PostNotFoundException {
        PostEntity post = postRepository.findById(postId).orElseThrow(()->new PostNotFoundException(POST_NOT_FOUND));
        return post.getUsersSaved().stream().anyMatch(e->e.getId().equals(userId));
    }

    @Override
    public void userUnsavePost(Long userId, Long postId) throws PostNotFoundException {
        PostEntity post = postRepository.findById(postId).orElseThrow(()->new PostNotFoundException(POST_NOT_FOUND));
        post.getUsersSaved().removeIf(e->e.getId().equals(userId));
        postRepository.save(post);
    }

}
