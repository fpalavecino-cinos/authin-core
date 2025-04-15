package org.cinos.authin_core.posts.dto.mapper;

import org.cinos.authin_core.posts.dto.PostDTO;
import org.cinos.authin_core.posts.dto.PostLocationDTO;
import org.cinos.authin_core.posts.entity.*;
import org.cinos.authin_core.users.entity.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(source = "images", target = "imagesUrls", qualifiedByName = "mapImages")
    @Mapping(source = "userAccount", target = "userFullName", qualifiedByName = "mapUserFullName")
    @Mapping(source = "userAccount", target = "userId", qualifiedByName = "mapUserId")
    @Mapping(source = "location", target = "location", qualifiedByName = "mapLocation")
    @Mapping(source = "userAccount", target = "userAvatar", qualifiedByName = "mapUserAvatar")
    @Mapping(source = "technicalVerification", target = "technicalVerification")
    PostDTO toDTO(PostEntity post);
    PostLocationDTO toLocationDTO(PostLocationEntity location);

    @Named("mapImages")
    default List<String> mapImages(List<PostImageEntity> images) {
        if (images == null) {
            return new ArrayList<>();
        }
        return images.stream()
                .map(PostImageEntity::getUrl)
                .toList();
    }

    @Named("mapUserFullName")
    default String mapUserFullName(AccountEntity account) {
        if (account == null || account.getUser() == null) {
            return null;
        }
        String firstName = account.getUser().getName() != null ? account.getUser().getName() : "";
        String lastName = account.getUser().getLastname() != null ? account.getUser().getLastname() : "";
        return (firstName + " " + lastName).trim();
    }

    @Named("mapUserId")
    default Long mapUserId(AccountEntity account) {
        if (account == null) {
            return null;
        }
        return account.getId();
    }

    @Named("mapLocation")
    default PostLocationDTO mapLocation(PostLocationEntity location) {
        return PostLocationDTO.builder()
                .address(location.getAddress())
                .lat(location.getLat())
                .lng(location.getLng())
                .build();
    }

    @Named("mapUserAvatar")
    default String mapUserAvatar(AccountEntity account) {
        return account.getAvatarImg();
    }

    @Named("mapCarModel")
    default String mapCarModel(ModelEntity model) {
        return model.getName();
    }
    @Named("mapCarMake")
    default String mapCarMake(MakeEntity make) {
        return make.getName();
    }

}


