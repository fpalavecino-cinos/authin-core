package org.cinos.authin_core.posts.service.impl;

import lombok.RequiredArgsConstructor;
import org.cinos.authin_core.posts.dto.ModelDTO;
import org.cinos.authin_core.posts.entity.ModelEntity;
import org.cinos.authin_core.posts.repository.ModelRepository;
import org.cinos.authin_core.posts.service.IModelService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModelService implements IModelService {
    private final ModelRepository modelRepository;

    @Override
    public Optional<ModelEntity> findByName(String name) {
        return modelRepository.findByName(name);
    }

    @Override
    public List<ModelDTO> findAllByMakeName(String makeName) {
        return modelRepository.findByMake_Name(makeName).stream().map(e->
                ModelDTO.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build()
        ).toList();
    }
}
