package org.cinos.authin_core.posts.service;

import org.cinos.authin_core.posts.dto.MakeDTO;
import org.cinos.authin_core.posts.entity.MakeEntity;

import java.util.List;
import java.util.Optional;

public interface IMakeService {
    Optional<MakeEntity> findByName(String name);
    List<MakeDTO> findByNameContainingIgnoreCase(String name);
}
