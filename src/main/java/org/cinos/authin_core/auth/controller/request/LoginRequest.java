package org.cinos.authin_core.auth.controller.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El usuario no puede estar vacio")
        String username,
        @NotBlank(message = "La contraseña no puede estar vacia")
        String password) {
}
