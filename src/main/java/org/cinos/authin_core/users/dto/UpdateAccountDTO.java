package org.cinos.authin_core.users.dto;

import org.springframework.web.multipart.MultipartFile;

public record UpdateAccountDTO(
        Long id,
        String name,
        String lastname) {
}
