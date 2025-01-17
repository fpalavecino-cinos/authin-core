package org.cinos.authin_core.posts.dto.mapper;

import org.cinos.authin_core.posts.dto.PostDTO;
import org.cinos.authin_core.posts.dto.PostLocationDTO;
import org.cinos.authin_core.posts.entity.PostEntity;
import org.cinos.authin_core.posts.entity.PostImageEntity;
import org.cinos.authin_core.posts.entity.PostLocationEntity;
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
    @Mapping(source = "location", target = "location", qualifiedByName = "mapLocation")
    PostDTO toDTO(PostEntity post);

    @Named("mapImages")
    default List<String> mapImages(List<PostImageEntity> images) {
        if (images == null) {
            return new ArrayList<>();
        }
        return images.stream()
                .map(PostImageEntity::getUrl) // Suponiendo que PostImageEntity tiene un método getUrl()
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

    @Named("mapLocation")
    default PostLocationDTO mapLocation(PostLocationEntity location) {
        return PostLocationDTO.builder()
                .lat(location.getLat())
                .lng(location.getLng())
                .build();
    }

}


