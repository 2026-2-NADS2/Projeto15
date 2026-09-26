package br.com.kfka.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name="vinculo_turma", uniqueConstraints=@UniqueConstraint(name="uk_vinculo", columnNames={"professor_id","disciplina_id","turma_id"}))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VinculoTurma {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message="professor é obrigatório") @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="professor_id", nullable=false)
    private Professor professor;
    @NotNull(message="disciplina é obrigatório") @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="disciplina_id", nullable=false)
    private Disciplina disciplina;
    @NotNull(message="turma é obrigatório") @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="turma_id", nullable=false)
    private Turma turma;
    @Column(nullable=false)
    private boolean ativo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }
    public Disciplina getDisciplina() { return disciplina; }
    public void setDisciplina(Disciplina disciplina) { this.disciplina = disciplina; }
    public Turma getTurma() { return turma; }
    public void setTurma(Turma turma) { this.turma = turma; }
    public boolean getAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
