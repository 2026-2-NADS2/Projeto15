package br.com.kfka.config;

import br.com.kfka.model.*;
import br.com.kfka.repository.UsuarioRepository;
import br.com.kfka.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import jakarta.validation.Validator;

@Component
@Profile({"dev", "bootstrap"})
public class AdministradorInicial implements CommandLineRunner {
    private final UsuarioRepository repository;
    private final UsuarioService service;
    private final Validator validator;
    @Value("${kfka.admin.email}") private String email;
    @Value("${kfka.admin.senha}") private String senha;
    public AdministradorInicial(UsuarioRepository repository, UsuarioService service, Validator validator) {
        this.repository = repository; this.service = service; this.validator = validator;
    }
    @Override public void run(String... args) {
        if (email.isBlank() && senha.isBlank()) return;
        if (repository.existsByPerfil(PerfilUsuario.ADMINISTRADOR)) return;
        UsuarioCadastro dados = new UsuarioCadastro("Administrador inicial", email, senha, PerfilUsuario.ADMINISTRADOR, true);
        if (!validator.validate(dados).isEmpty()) throw new IllegalArgumentException("Configure ADMIN_EMAIL válido e ADMIN_PASSWORD entre 8 e 72 caracteres");
        service.salvar(null, dados);
    }
}
