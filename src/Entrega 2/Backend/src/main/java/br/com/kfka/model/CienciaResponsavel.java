package br.com.kfka.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name="ciencia_responsavel", uniqueConstraints=@UniqueConstraint(name="uk_ciencia", columnNames={"responsavel_id","acompanhamento_id"}))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CienciaResponsavel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message="responsavel é obrigatório") @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="responsavel_id", nullable=false)
    private Responsavel responsavel;
    @JsonIgnore @ManyToOne @JoinColumn(name="acompanhamento_id", nullable=false)
    private Acompanhamento acompanhamento;
    @Column(nullable=false)
    private LocalDateTime dataHoraCiencia;
    @Size(max=1000) @Column(length=1000)
    private String observacao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Responsavel getResponsavel() { return responsavel; }
    public void setResponsavel(Responsavel responsavel) { this.responsavel = responsavel; }
    public Acompanhamento getAcompanhamento() { return acompanhamento; }
    public void setAcompanhamento(Acompanhamento acompanhamento) { this.acompanhamento = acompanhamento; }
    public LocalDateTime getDataHoraCiencia() { return dataHoraCiencia; }
    public void setDataHoraCiencia(LocalDateTime dataHoraCiencia) { this.dataHoraCiencia = dataHoraCiencia; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
