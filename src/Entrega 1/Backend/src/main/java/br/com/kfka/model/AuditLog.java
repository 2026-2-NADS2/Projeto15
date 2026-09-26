package br.com.kfka.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="audit_log", indexes=@Index(name="idx_audit_data", columnList="data_hora"))
public class AuditLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="data_hora", nullable=false) private LocalDateTime dataHora;
    @Column(name="usuario_id", nullable=false) private Long usuarioId;
    @Column(nullable=false, length=150) private String usuario;
    @Column(nullable=false, length=20) private String metodo;
    @Column(nullable=false, length=500) private String recurso;
    @Column(nullable=false) private Integer status;
    public Long getId() { return id; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public String getRecurso() { return recurso; }
    public void setRecurso(String recurso) { this.recurso = recurso; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
