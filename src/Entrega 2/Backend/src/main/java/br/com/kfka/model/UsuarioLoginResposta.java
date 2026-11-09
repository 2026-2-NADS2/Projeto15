package br.com.kfka.model;

public record UsuarioLoginResposta(Long id, String nome, String email, PerfilUsuario perfil, String token) {}
