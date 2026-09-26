package br.com.kfka.model;

import jakarta.validation.constraints.*;

public record FiltroRelatorio(
    @Positive Long turmaId, @Positive Long disciplinaId, @Positive Long professorId,
    @Positive Long bimestreId, @Positive Long alunoId, @Positive Long tagId,
    @Min(2000) @Max(2100) Integer anoLetivo, @Min(1) @Max(4) Integer bimestre,
    StatusAcompanhamento status
) {}
