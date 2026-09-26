package br.com.kfka.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name="matricula", uniqueConstraints=@UniqueConstraint(name="uk_matricula", columnNames={"aluno_id","turma_id"}))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Matricula {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message="aluno é obrigatório") @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="aluno_id", nullable=false)
    private Aluno aluno;
    @NotNull(message="turma é obrigatório") @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="turma_id", nullable=false)
    private Turma turma;
    @Column(nullable=false)
    private boolean ativo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public Turma getTurma() { return turma; }
    public void setTurma(Turma turma) { this.turma = turma; }
    public boolean getAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
