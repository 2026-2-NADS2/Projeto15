package br.com.kfka.model;

import jakarta.validation.constraints.*;
import java.util.Set;

public class DadosCadastro {
    public record DadosProfessor(@NotNull @Positive Long usuarioId) {}
    public record DadosResponsavel(@NotNull @Positive Long usuarioId, @NotNull Set<@NotNull @Positive Long> alunosIds) {}
    public record DadosDisciplina(@NotBlank @Size(max=150) String nome, @NotNull @Positive Long areaDisciplinaId, boolean ativo) {}
    public record DadosMatricula(@NotNull @Positive Long alunoId, @NotNull @Positive Long turmaId, boolean ativo) {}
    public record DadosVinculo(@NotNull @Positive Long professorId, @NotNull @Positive Long disciplinaId, @NotNull @Positive Long turmaId, boolean ativo) {}
}
