package br.com.kfka.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name="responsavel")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Responsavel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull @OneToOne @JoinColumn(name="usuario_id", nullable=false, unique=true)
    private Usuario usuario;
    @ManyToMany(fetch=FetchType.EAGER) @JoinTable(name="responsavel_aluno", joinColumns=@JoinColumn(name="responsavel_id"), inverseJoinColumns=@JoinColumn(name="aluno_id"), uniqueConstraints=@UniqueConstraint(columnNames={"responsavel_id","aluno_id"}))
    private Set<Aluno> alunos = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Set<Aluno> getAlunos() { return alunos; }
    public void setAlunos(Set<Aluno> alunos) { this.alunos = alunos; }
}
