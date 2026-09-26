package br.com.kfka.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name="disciplina")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Disciplina {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message="Nome é obrigatório") @Size(max=150, message="Nome deve ter até 150 caracteres") @Column(nullable=false, length=150)
    private String nome;
    @NotNull @ManyToOne @JoinColumn(name="area_disciplina_id", nullable=false)
    private AreaDisciplina areaDisciplina;
    @Column(nullable=false)
    private boolean ativo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public AreaDisciplina getAreaDisciplina() { return areaDisciplina; }
    public void setAreaDisciplina(AreaDisciplina areaDisciplina) { this.areaDisciplina = areaDisciplina; }
    public boolean getAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
