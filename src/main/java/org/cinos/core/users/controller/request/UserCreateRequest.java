package org.cinos.core.users.controller.request;

import lombok.Builder;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

@Builder
public record UserCreateRequest(
        @NotBlank(message = "El usuario no puede estar vacio")
        String username,
        @NotBlank(message = "El nombre no puede estar vacio")
        String name,
        @NotBlank(message = "El apellido no puede estar vacio")
        String lastname,
        @NotBlank(message = "El email no puede estar vacio")
        String email,
        @NotBlank(message = "La contraseña no puede estar vacia")
        String password,
        @NotBlank(message = "La contraseña no puede estar vacia")
        String repeatPassword) implements Serializable {
}
