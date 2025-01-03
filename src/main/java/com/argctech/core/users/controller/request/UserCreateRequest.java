package com.argctech.core.users.controller.request;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record UserCreateRequest(String username, String name, String lastname ,String email, String password, String repeatPassword) implements Serializable {
}
