package br.com.kfka.model;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="recuperacao_senha_token",indexes=@Index(name="idx_recuperacao_token",columnList="token_hash",unique=true))
public class RecuperacaoSenhaToken {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="usuario_id",nullable=false) private Usuario usuario;
 @Column(name="token_hash",nullable=false,length=64,unique=true) private String tokenHash;
 @Column(name="expira_em",nullable=false) private LocalDateTime expiraEm; @Column(name="usado_em") private LocalDateTime usadoEm;
 public Long getId(){return id;} public Usuario getUsuario(){return usuario;} public void setUsuario(Usuario v){usuario=v;}
 public String getTokenHash(){return tokenHash;} public void setTokenHash(String v){tokenHash=v;}
 public LocalDateTime getExpiraEm(){return expiraEm;} public void setExpiraEm(LocalDateTime v){expiraEm=v;}
 public LocalDateTime getUsadoEm(){return usadoEm;} public void setUsadoEm(LocalDateTime v){usadoEm=v;}
}
