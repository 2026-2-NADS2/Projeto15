package br.com.kfka.controller;

import br.com.kfka.model.Aluno;
import br.com.kfka.service.CadastroService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;

@io.swagger.v3.oas.annotations.tags.Tag(name="Aluno")
@RestController
@RequestMapping("/alunos")
public class AlunoController {
    private final CadastroService service;
    @org.springframework.beans.factory.annotation.Autowired private br.com.kfka.service.AcompanhamentoService acompanhamentos;
    public AlunoController(CadastroService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<Page<Aluno>> listar(@RequestParam(defaultValue="") String nome, Pageable pageable) { return ResponseEntity.ok(service.listarAluno(nome, pageable)); }

    @GetMapping("/{id}")
    public ResponseEntity<Aluno> buscar(@PathVariable Long id) { return ResponseEntity.ok(service.buscarAluno(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Aluno> cadastrar(@Valid @RequestBody Aluno dados) {
        Aluno criado = service.salvarAluno(null, dados);
        return ResponseEntity.created(URI.create("/alunos/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Aluno> atualizar(@PathVariable Long id, @Valid @RequestBody Aluno dados) {
        return ResponseEntity.ok(service.salvarAluno(id, dados));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) { service.inativarAluno(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/{id}/acompanhamentos")
    public ResponseEntity<Page<br.com.kfka.model.AcompanhamentoResposta>> acompanhamentos(@PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(acompanhamentos.listarPorAluno(id, pageable));
    }
}
