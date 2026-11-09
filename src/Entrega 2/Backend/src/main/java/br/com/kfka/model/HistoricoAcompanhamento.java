package br.com.kfka.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name="historico_acompanhamento")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HistoricoAcompanhamento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonIgnore @ManyToOne @JoinColumn(name="acompanhamento_id", nullable=false)
    private Acompanhamento acompanhamento;
    @NotNull(message="usuarioResponsavelPelaAcao é obrigatório") @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="usuarioResponsavelPelaAcao_id", nullable=false)
    private Usuario usuarioResponsavelPelaAcao;
    @Column(nullable=false)
    private LocalDateTime dataHora;
    @Enumerated(EnumType.STRING) @Column(length=30)
    private StatusAcompanhamento statusAnterior;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=30)
    private StatusAcompanhamento statusNovo;
    @Column(nullable=false, length=1000)
    private String acao;
    @Column(columnDefinition="LONGTEXT")
    private String dadosAnteriores;
    @Column(columnDefinition="LONGTEXT")
    private String dadosNovos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Acompanhamento getAcompanhamento() { return acompanhamento; }
    public void setAcompanhamento(Acompanhamento acompanhamento) { this.acompanhamento = acompanhamento; }
    public Usuario getUsuarioResponsavelPelaAcao() { return usuarioResponsavelPelaAcao; }
    public void setUsuarioResponsavelPelaAcao(Usuario usuarioResponsavelPelaAcao) { this.usuarioResponsavelPelaAcao = usuarioResponsavelPelaAcao; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public StatusAcompanhamento getStatusAnterior() { return statusAnterior; }
    public void setStatusAnterior(StatusAcompanhamento statusAnterior) { this.statusAnterior = statusAnterior; }
    public StatusAcompanhamento getStatusNovo() { return statusNovo; }
    public void setStatusNovo(StatusAcompanhamento statusNovo) { this.statusNovo = statusNovo; }
    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }
    public String getDadosAnteriores() { return dadosAnteriores; }
    public void setDadosAnteriores(String dadosAnteriores) { this.dadosAnteriores = dadosAnteriores; }
    public String getDadosNovos() { return dadosNovos; }
    public void setDadosNovos(String dadosNovos) { this.dadosNovos = dadosNovos; }
}
