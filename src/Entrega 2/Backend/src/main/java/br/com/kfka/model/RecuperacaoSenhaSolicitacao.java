package br.com.kfka.model;
import jakarta.validation.constraints.*;
public record RecuperacaoSenhaSolicitacao(@NotBlank @Email @Size(max=254) String email) {}
