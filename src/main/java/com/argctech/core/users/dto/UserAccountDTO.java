package com.argctech.core.users.dto;

import com.argctech.core.users.model.Role;

public record UserAccountDTO(
        String name,
        String username,
        String lastname,
        String email,
        String phone,
        Role role) { }
