package br.com.kfka.model;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UsuarioCadastro(
    @NotBlank @Size(max=150) String nome,
    @NotBlank @Email(message="E-mail inválido") @Size(max=254) String email,
    @NotBlank @Size(min=8, max=72, message="Senha deve ter entre 8 e 72 caracteres")
    @JsonProperty(access=JsonProperty.Access.WRITE_ONLY) String senha,
    @NotNull PerfilUsuario perfil,
    boolean ativo
) {}
