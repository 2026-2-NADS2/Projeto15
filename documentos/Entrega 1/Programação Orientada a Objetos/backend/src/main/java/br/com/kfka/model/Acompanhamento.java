package br.com.kfka.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name="acompanhamento", uniqueConstraints=@UniqueConstraint(name="uk_acompanhamento", columnNames={"aluno_id","turma_id","disciplina_id","professor_id","bimestre_id"}), indexes={@Index(name="idx_acomp_status", columnList="status"), @Index(name="idx_acomp_aluno", columnList="aluno_id"), @Index(name="idx_acomp_professor", columnList="professor_id"), @Index(name="idx_acomp_turma", columnList="turma_id"), @Index(name="idx_acomp_disciplina", columnList="disciplina_id"), @Index(name="idx_acomp_bimestre", columnList="bimestre_id")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Acompanhamento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message="aluno é obrigatório") @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="aluno_id", nullable=false)
    private Aluno aluno;
    @NotNull(message="turma é obrigatório") @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="turma_id", nullable=false)
    private Turma turma;
    @NotNull(message="disciplina é obrigatório") @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="disciplina_id", nullable=false)
    private Disciplina disciplina;
    @NotNull(message="professor é obrigatório") @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="professor_id", nullable=false)
    private Professor professor;
    @NotNull(message="bimestre é obrigatório") @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="bimestre_id", nullable=false)
    private Bimestre bimestre;
    @NotBlank @Size(max=10000) @Column(nullable=false, length=10000)
    private String descricao;
    @NotNull @Column(nullable=false, precision=7, scale=2)
    private BigDecimal media;
    @ManyToMany(fetch=FetchType.EAGER) @JoinTable(name="acompanhamento_tag", joinColumns=@JoinColumn(name="acompanhamento_id"), inverseJoinColumns=@JoinColumn(name="tag_id"), uniqueConstraints=@UniqueConstraint(columnNames={"acompanhamento_id","tag_id"}))
    private Set<Tag> tags = new HashSet<>();
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=30)
    private StatusAcompanhamento status = StatusAcompanhamento.RASCUNHO;
    @Column(nullable=false, updatable=false)
    private LocalDateTime dataCriacao;
    @Column(nullable=false)
    private LocalDateTime dataAtualizacao;
    @JsonIgnore @Version
    private Long versao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public Turma getTurma() { return turma; }
    public void setTurma(Turma turma) { this.turma = turma; }
    public Disciplina getDisciplina() { return disciplina; }
    public void setDisciplina(Disciplina disciplina) { this.disciplina = disciplina; }
    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }
    public Bimestre getBimestre() { return bimestre; }
    public void setBimestre(Bimestre bimestre) { this.bimestre = bimestre; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getMedia() { return media; }
    public void setMedia(BigDecimal media) { this.media = media; }
    public Set<Tag> getTags() { return tags; }
    public void setTags(Set<Tag> tags) { this.tags = tags; }
    public StatusAcompanhamento getStatus() { return status; }
    public void setStatus(StatusAcompanhamento status) { this.status = status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
}
