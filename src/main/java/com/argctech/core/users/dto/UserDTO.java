package com.argctech.core.users.dto;

import com.argctech.core.users.model.Role;

public record UserDTO(
        Long id,
String name,
String username,
String lastname,
String email,
String phone,
Boolean active,
Role role) { }
