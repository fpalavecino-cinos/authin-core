package org.cinos.authin_core.users.dto;

import org.cinos.authin_core.users.model.Role;

public record UserAccountDTO(
        String name,
        String username,
        String lastname,
        String email,
        String phone,
        Role role) { }
