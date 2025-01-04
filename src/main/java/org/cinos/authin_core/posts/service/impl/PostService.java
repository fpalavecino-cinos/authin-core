package org.cinos.authin_core.posts.service.impl;

import org.cinos.authin_core.posts.controller.request.PostCreateRequest;
import org.cinos.authin_core.posts.dto.PostDTO;
import org.cinos.authin_core.posts.dto.PostFeedDTO;
import org.cinos.authin_core.posts.entity.PostEntity;
import org.cinos.authin_core.posts.entity.PostImageEntity;
import org.cinos.authin_core.posts.repository.PostImageRepository;
import org.cinos.authin_core.posts.repository.PostRepository;
import org.cinos.authin_core.posts.repository.specs.PostSpecifications;
import org.cinos.authin_core.posts.service.IPostService;
import org.cinos.authin_core.posts.utils.exceptions.PostNotFoundException;
import org.cinos.authin_core.follows.service.IFollowService;
import org.cinos.authin_core.users.dto.DTOConverter;
import org.cinos.authin_core.users.dto.UserDTO;
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
//    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String POST_NOT_FOUND = "La publicacion no se encontró";
    private final UserService userService;
    private final StorageService storageService;
    private final PostImageRepository postImageRepository;

    @Override
    public List<PostDTO> getPostPageable(Integer page, Integer size) {
        List<PostEntity> entityList = postRepository.findAll(PageRequest.of(page, size)).toList();
        return entityList.stream().map(e -> DTOConverter.toDTO(e, PostDTO.class)).toList();
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
                userFullName = userService.getFullName(e.getUserId());
            } catch (UserNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            return PostFeedDTO.builder()
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
        return postRepository.findAllByUserIdInOrderByPublicationDateDesc(followingsIds, PostDTO.class, pageable);
    }

    @Override
    public PostDTO getById(Long id) throws PostNotFoundException {
        return postRepository.findById(id, PostDTO.class).orElseThrow(()->new PostNotFoundException(POST_NOT_FOUND));
    }

    @Override
    public List<PostDTO> getByUserId(Long userId) {
        return postRepository.findByUserId(userId, PostDTO.class);
    }

    @Override
    public PostDTO createPost(PostCreateRequest request, List<MultipartFile> images) throws IOException {
        PostEntity postEntity = PostEntity.builder()
                .make(request.make())
                .model(request.model())
                .year(request.year())
                .isUsed(request.isUsed())
                .price(request.price())
                .description(request.description())
                .userId(request.userId())
                .publicationDate(LocalDateTime.now())
                .active(request.active())
                .currencySymbol(request.currencySymbol())
                .build();

        List<String> imageUrls = storageService.uploadFiles(images);
        List<PostImageEntity> imagesEntity = imageUrls.stream().map(url -> PostImageEntity.builder()
                .url(url)
                .post(postEntity)
                .build()).toList();

        postEntity.setImages(imagesEntity);
        postRepository.save(postEntity);
        postImageRepository.saveAll(imagesEntity);
        return DTOConverter.toDTO(postEntity, PostDTO.class);
    }

}
