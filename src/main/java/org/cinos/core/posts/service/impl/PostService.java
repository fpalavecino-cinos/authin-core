package org.cinos.core.posts.service.impl;

import org.cinos.core.mail.models.SendEmailRequest;
import org.cinos.core.mail.service.MailService;
import org.cinos.core.posts.controller.request.PostCreateRequest;
import org.cinos.core.posts.dto.PostDTO;
import org.cinos.core.posts.dto.PostFeedDTO;
import org.cinos.core.posts.dto.PostFilterDTO;
import org.cinos.core.posts.dto.PostProfileDTO;
import org.cinos.core.posts.dto.mapper.PostMapper;
import org.cinos.core.posts.entity.*;
import org.cinos.core.posts.models.DocumentationStatus;
import org.cinos.core.posts.models.VerificationStatus;
import org.cinos.core.posts.repository.PostImageRepository;
import org.cinos.core.posts.repository.PostLocationRepository;
import org.cinos.core.posts.repository.PostRepository;
import org.cinos.core.technical_verification.repository.TechnicalVerificationRepository;
import org.cinos.core.posts.repository.specs.PostSpecifications;
import org.cinos.core.posts.service.IMakeService;
import org.cinos.core.posts.service.IModelService;
import org.cinos.core.posts.service.IPostService;
import org.cinos.core.posts.utils.exceptions.PostNotFoundException;
import org.cinos.core.follows.service.IFollowService;
import org.cinos.core.technical_verification.entity.TechnicalVerification;
import org.cinos.core.users.dto.UserDTO;
import org.cinos.core.users.service.impl.AccountService;
import org.cinos.core.users.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService implements IPostService {

    private final PostRepository postRepository;
    private final IFollowService followService;
    private final String POST_NOT_FOUND = "La publicacion no se encontró";
    private final StorageService storageService;
    private final PostImageRepository postImageRepository;
    private final PostLocationRepository postLocationRepository;
    private final AccountService accountService;
    private final PostMapper postMapper;
    private final IMakeService makeService;
    private final IModelService modelService;
    private final MailService mailService;
    private final TechnicalVerificationRepository technicalVerificationRepository;

    @Override
    public List<PostDTO> getPostPageable(Integer page, Integer size) {
        List<PostEntity> entityList = postRepository.findAll(PageRequest.of(page, size)).toList();
        return entityList.stream().map(postMapper::toDTO).toList();
    }

    @Override
    public Page<PostFeedDTO> getFeedPosts(
            Long userId,
            Pageable pageable,
            Double userLatitude,
            Double userLongitude
    ) throws UserNotFoundException {
        List<UserDTO> followings = followService.getFollowings(userId);
        List<Long> followingsIds = followings.stream().map(UserDTO::id).toList();

        // Especificación con timeFactor, comentarios, ubicación y relación
        Specification<PostEntity> spec = PostSpecifications.postFeedSpec(
                followingsIds,
                userLatitude,
                userLongitude,
                userId
        );

        // Obtener publicaciones paginadas y ordenadas por relevancia
        Page<PostEntity> postEntityPage = postRepository.findAll(spec, pageable);

        // Mapear a DTO
        return postEntityPage.map((e) -> {
            String userFullName = e.getUserAccount().getUser().getName() + " " + e.getUserAccount().getUser().getLastname();
            return PostFeedDTO.builder()
                    .id(e.getId())
                    .model(e.getModel())
                    .price(e.getPrice())
                    .year(e.getYear())
                    .make(e.getMake())
                    .isUsed(e.getIsUsed())
                    .userFullName(userFullName)
                    .publicationDate(e.getPublicationDate())
                    .imagesUrls(e.getImages().stream().map(PostImageEntity::getUrl).toList())
                    .currencySymbol(e.getCurrencySymbol())
                    .location(postMapper.toLocationDTO(e.getLocation()))
                    .kilometers(e.getKilometers())
                    .userId(e.getUserAccount().getId())
                    .isVerified(e.getIsVerified())
                    .build();
        });
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
        makeService.findByName(request.make()).orElseThrow(() -> new RuntimeException("Marca no encontrada"));
        modelService.findByName(request.model()).orElseThrow(() -> new RuntimeException("Modelo no encontrado"));

        PostEntity postEntity = PostEntity.builder()
                .model(request.model())
                .make(request.make())
                .kilometers(request.kilometers())
                .fuel(request.fuel())
                .transmission(request.transmission())
                .year(request.year())
                .isUsed(request.isUsed())
                .price(request.price())
                .userAccount(accountService.getAccountEntityById(request.userId()))
                .publicationDate(LocalDateTime.now())
                .active(Boolean.TRUE)
                .currencySymbol(request.currencySymbol())
                .documentationStatus(DocumentationStatus.NOT_PROVIDED)
                .build();

        TechnicalVerification technicalVerification = TechnicalVerification.builder()
                .post(postEntity)
                .status(VerificationStatus.NOT_STARTED)
                .build();
        List<String> imageUrls = storageService.uploadFiles(images);
        List<PostImageEntity> imagesEntity = imageUrls.stream().map(url -> PostImageEntity.builder()
                .url(url)
                .post(postEntity)
                .build()).toList();

        PostLocationEntity location = PostLocationEntity.builder()
                .address(request.location().address())
                .lat(request.location().lat())
                .lng(request.location().lng())
                .post(postEntity)
                .build();

        postEntity.setImages(imagesEntity);
        postEntity.setLocation(location);
        postRepository.save(postEntity);
        technicalVerificationRepository.save(technicalVerification);
        postImageRepository.saveAll(imagesEntity);
        postLocationRepository.save(location);
        return postMapper.toDTO(postEntity);
    }

    @Override
    public List<PostProfileDTO> getPostsProfile(Long userId) throws UserNotFoundException {
        List<PostEntity> posts = postRepository.findAllByUserAccount_IdAndActiveTrue(userId);
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
    public List<PostProfileDTO> getSavedPostsProfile(final Long userId) {
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

    @Override
    public void deactivatePost(Long postId) throws PostNotFoundException {
        PostEntity post = postRepository.findById(postId).orElseThrow(()->new PostNotFoundException(POST_NOT_FOUND));
        post.setActive(Boolean.FALSE);
        postRepository.save(post);
    }

    @Override
    public void uploadDocumentation(Long postId, List<MultipartFile> files) throws PostNotFoundException {
        PostEntity post = postRepository.findById(postId).orElseThrow(()->new PostNotFoundException(POST_NOT_FOUND));
        String message = """
                Se ha subido la documentación para la publicación de %s %s del año %s, %s km
                """.formatted(post.getMake(), post.getModel(), post.getYear(), post.getKilometers());
        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .to(new String[]{"fpalavecino@cinos.org"})
                .subject("Documentación de publicación:" + post.getId())
                .message(message)
                .attachments(files)
                .build();
        try {
            mailService.sendMail(sendEmailRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar el correo", e);
        }
    }

    @Override
    public Page<PostDTO> getPostsFilter(PostFilterDTO postFilterDTO) {
        Page<PostEntity> postPage = postRepository.findAll(PostSpecifications.postFilterSpec(postFilterDTO), PageRequest.of(postFilterDTO.page(), postFilterDTO.size()));
        return postPage.map(postMapper::toDTO);
    }


}
