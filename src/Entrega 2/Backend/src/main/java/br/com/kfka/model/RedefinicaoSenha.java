package br.com.kfka.model;
import jakarta.validation.constraints.*; import com.fasterxml.jackson.annotation.JsonProperty;
public record RedefinicaoSenha(@NotBlank @Size(max=100) String codigo,@NotBlank @Size(min=8,max=72) @Pattern(regexp="^(?=.*\\p{L})(?=.*\\d).*$", message="Senha deve ter pelo menos uma letra e um número") @JsonProperty(access=JsonProperty.Access.WRITE_ONLY) String novaSenha) {}
