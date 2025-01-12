package org.cinos.authin_core.posts.dto.mapper;

import org.cinos.authin_core.posts.dto.PostDTO;
import org.cinos.authin_core.posts.entity.PostEntity;
import org.cinos.authin_core.posts.entity.PostImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(source = "images", target = "imagesUrls", qualifiedByName = "mapImages")
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


}


