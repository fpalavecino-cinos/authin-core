package org.cinos.authin_core.posts.service;

import org.cinos.authin_core.posts.dto.ModelDTO;
import org.cinos.authin_core.posts.entity.ModelEntity;

import java.util.List;
import java.util.Optional;

public interface IModelService {
    Optional<ModelEntity> findByName(String name);
    List<ModelDTO> findAllByMakeName(String makeName);
}
