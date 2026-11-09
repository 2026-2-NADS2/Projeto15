package br.com.kfka.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record AcompanhamentoResposta(Long id, Aluno aluno, Turma turma, Disciplina disciplina,
    ProfessorResumo professor, Bimestre bimestre, String descricao, BigDecimal media,
    Set<Tag> tags, StatusAcompanhamento status, LocalDateTime dataCriacao, LocalDateTime dataAtualizacao) {
    public record ProfessorResumo(Long id, String nome) {}
    public static AcompanhamentoResposta de(Acompanhamento a) {
        return new AcompanhamentoResposta(a.getId(), a.getAluno(), a.getTurma(), a.getDisciplina(),
            new ProfessorResumo(a.getProfessor().getId(), a.getProfessor().getUsuario().getNome()), a.getBimestre(),
            a.getDescricao(), a.getMedia(), a.getTags(), a.getStatus(), a.getDataCriacao(), a.getDataAtualizacao());
    }
}
