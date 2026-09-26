package br.com.kfka.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name="turma", indexes=@Index(name="idx_turma_ano", columnList="ano_letivo"))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Turma {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message="Nome é obrigatório") @Size(max=150, message="Nome deve ter até 150 caracteres") @Column(nullable=false, length=150)
    private String nome;
    @NotBlank @Size(max=50) @Column(nullable=false, length=50)
    private String serie;
    @NotNull @Min(2000) @Max(2100) @Column(name="ano_letivo", nullable=false)
    private Integer anoLetivo;
    @Column(nullable=false)
    private boolean ativo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    public Integer getAnoLetivo() { return anoLetivo; }
    public void setAnoLetivo(Integer anoLetivo) { this.anoLetivo = anoLetivo; }
    public boolean getAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
