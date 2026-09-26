package br.com.kfka.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name="usuario")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message="Nome é obrigatório") @Size(max=150, message="Nome deve ter até 150 caracteres") @Column(nullable=false, length=150)
    private String nome;
    @NotBlank @Email(message="E-mail inválido") @Size(max=254) @Column(nullable=false, unique=true, length=254)
    private String email;
    @JsonProperty(access=JsonProperty.Access.WRITE_ONLY) @Column(nullable=false, length=100)
    private String senha;
    @NotNull @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
    private PerfilUsuario perfil;
    @Column(nullable=false)
    private boolean ativo = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public PerfilUsuario getPerfil() { return perfil; }
    public void setPerfil(PerfilUsuario perfil) { this.perfil = perfil; }
    public boolean getAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
