package br.com.kfka.model;
import jakarta.validation.constraints.*; import com.fasterxml.jackson.annotation.JsonProperty;
public record RedefinicaoSenha(@NotBlank @Size(max=100) String codigo,@NotBlank @Size(min=8,max=72) @JsonProperty(access=JsonProperty.Access.WRITE_ONLY) String novaSenha) {}
