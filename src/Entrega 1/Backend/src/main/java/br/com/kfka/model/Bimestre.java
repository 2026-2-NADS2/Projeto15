package br.com.kfka.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name="bimestre", uniqueConstraints=@UniqueConstraint(name="uk_bimestre", columnNames={"ano_letivo","numero"}))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Bimestre {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull @Min(value=1, message="Bimestre deve estar entre 1 e 4") @Max(value=4, message="Bimestre deve estar entre 1 e 4") @Column(nullable=false)
    private Integer numero;
    @NotNull @Min(2000) @Max(2100) @Column(name="ano_letivo", nullable=false)
    private Integer anoLetivo;
    @NotNull @Column(nullable=false)
    private LocalDateTime dataHoraAbertura;
    @NotNull @Column(nullable=false)
    private LocalDateTime dataHoraEncerramento;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public Integer getAnoLetivo() { return anoLetivo; }
    public void setAnoLetivo(Integer anoLetivo) { this.anoLetivo = anoLetivo; }
    public LocalDateTime getDataHoraAbertura() { return dataHoraAbertura; }
    public void setDataHoraAbertura(LocalDateTime dataHoraAbertura) { this.dataHoraAbertura = dataHoraAbertura; }
    public LocalDateTime getDataHoraEncerramento() { return dataHoraEncerramento; }
    public void setDataHoraEncerramento(LocalDateTime dataHoraEncerramento) { this.dataHoraEncerramento = dataHoraEncerramento; }
}
