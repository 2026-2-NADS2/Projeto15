package br.com.kfka.controller;

import br.com.kfka.model.*;
import br.com.kfka.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@io.swagger.v3.oas.annotations.tags.Tag(name="Usuario")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService service;
    private final br.com.kfka.service.RecuperacaoSenhaService recuperacao;
    public UsuarioController(UsuarioService service, br.com.kfka.service.RecuperacaoSenhaService recuperacao) { this.service = service; this.recuperacao = recuperacao; }

    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @PostMapping("/logar")
    public ResponseEntity<UsuarioLoginResposta> logar(@Valid @RequestBody UsuarioLogin dados) { return ResponseEntity.ok(service.logar(dados)); }
    @PostMapping("/recuperacao-senha")
    public RecuperacaoSenhaResposta solicitarRecuperacao(@Valid @RequestBody RecuperacaoSenhaSolicitacao dados){return recuperacao.solicitar(dados.email());}
    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@Valid @RequestBody RedefinicaoSenha dados){recuperacao.redefinir(dados);return ResponseEntity.noContent().build();}

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Page<Usuario>> listar(@RequestParam(defaultValue="") String nome, Pageable pageable) { return ResponseEntity.ok(service.listar(nome, pageable)); }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Usuario> buscar(@PathVariable Long id) { return ResponseEntity.ok(service.buscar(id)); }

    @PostMapping({"", "/cadastrar"})
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Usuario> cadastrar(@Valid @RequestBody UsuarioCadastro dados) {
        Usuario criado = service.salvar(null, dados);
        return ResponseEntity.created(URI.create("/usuarios/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Usuario> atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioCadastro dados) { return ResponseEntity.ok(service.salvar(id, dados)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) { service.inativar(id); return ResponseEntity.noContent().build(); }
}
