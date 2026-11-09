package br.com.kfka.model;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UsuarioLogin(
    @NotBlank @Email String email,
    @NotBlank @Size(max=72) @JsonProperty(access=JsonProperty.Access.WRITE_ONLY) String senha
) {}
