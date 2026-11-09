package br.com.kfka.controller;

import br.com.kfka.model.Turma;
import br.com.kfka.service.CadastroService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;

@io.swagger.v3.oas.annotations.tags.Tag(name="Turma")
@RestController
@RequestMapping("/turmas")
public class TurmaController {
    private final CadastroService service;
    public TurmaController(CadastroService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<Page<Turma>> listar(Pageable pageable) { return ResponseEntity.ok(service.listarTurma(pageable)); }

    @GetMapping("/{id}")
    public ResponseEntity<Turma> buscar(@PathVariable Long id) { return ResponseEntity.ok(service.buscarTurma(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Turma> cadastrar(@Valid @RequestBody Turma dados) {
        Turma criado = service.salvarTurma(null, dados);
        return ResponseEntity.created(URI.create("/turmas/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Turma> atualizar(@PathVariable Long id, @Valid @RequestBody Turma dados) {
        return ResponseEntity.ok(service.salvarTurma(id, dados));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) { service.inativarTurma(id); return ResponseEntity.noContent().build(); }
}
