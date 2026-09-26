package br.com.kfka.model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Set;

public record AcompanhamentoCadastro(
    @NotNull @Positive Long alunoId,
    @NotNull @Positive Long turmaId,
    @NotNull @Positive Long disciplinaId,
    @NotNull @Positive Long bimestreId,
    @NotBlank @Size(max=10000) String descricao,
    @NotNull @Digits(integer=5, fraction=2) BigDecimal media,
    @NotNull @Size(max=100) Set<@NotNull @Positive Long> tagsIds
) {}
