package br.com.kfka.service;

import br.com.kfka.model.*;
import br.com.kfka.repository.*;
import br.com.kfka.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@Transactional
public class UsuarioService {
    private final UsuarioRepository repository;
    private final ProfessorRepository professores;
    private final ResponsavelRepository responsaveis;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final String hashInexistente;

    public UsuarioService(UsuarioRepository repository, ProfessorRepository professores,
                          ResponsavelRepository responsaveis, PasswordEncoder encoder, JwtService jwt) {
        this.repository = repository;
        this.professores = professores;
        this.responsaveis = responsaveis;
        this.encoder = encoder;
        this.jwt = jwt;
        this.hashInexistente = encoder.encode(java.util.UUID.randomUUID().toString());
    }

    @Transactional(readOnly=true)
    public Page<Usuario> listar(String nome, Pageable pageable) {
        return repository.findAllByNomeContainingIgnoreCase(nome, pageable);
    }

    @Transactional(readOnly=true)
    public Usuario buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    public Usuario salvar(Long id, UsuarioCadastro dados) {
        if (dados.senha().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha deve ter até 72 bytes UTF-8");
        String email = dados.email().trim().toLowerCase(Locale.ROOT);
        boolean duplicado = id == null ? repository.existsByEmail(email) : repository.existsByEmailAndIdNot(email, id);
        if (duplicado) throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        Usuario usuario = id == null ? new Usuario() : buscar(id);
        if (id != null && usuario.getPerfil() != dados.perfil()
            && (professores.existsByUsuarioId(id) || responsaveis.existsByUsuarioId(id)))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Perfil não pode ser alterado enquanto houver cadastro de professor ou responsável");
        usuario.setNome(dados.nome());
        usuario.setEmail(email);
        usuario.setSenha(encoder.encode(dados.senha()));
        usuario.setPerfil(dados.perfil());
        usuario.setAtivo(dados.ativo());
        return repository.saveAndFlush(usuario);
    }

    public void inativar(Long id) { buscar(id).setAtivo(false); }

    @Transactional(readOnly=true)
    public UsuarioLoginResposta logar(UsuarioLogin dados) {
        Usuario usuario = repository.findByEmail(dados.email().trim().toLowerCase(Locale.ROOT)).orElse(null);
        boolean senhaValida = dados.senha().getBytes(StandardCharsets.UTF_8).length <= 72
            && encoder.matches(dados.senha(), usuario == null ? hashInexistente : usuario.getSenha());
        if (usuario == null || !senhaValida || !usuario.getAtivo())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
        return new UsuarioLoginResposta(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil(), jwt.generateToken(usuario));
    }
}
