package br.com.kfka.controller;

import br.com.kfka.model.Disciplina;
import br.com.kfka.model.DadosCadastro.DadosDisciplina;
import br.com.kfka.service.RelacionamentoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;

@io.swagger.v3.oas.annotations.tags.Tag(name="Disciplina")
@RestController
@RequestMapping("/disciplinas")
public class DisciplinaController {
    private final RelacionamentoService service;
    public DisciplinaController(RelacionamentoService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<Page<Disciplina>> listar(Pageable pageable) { return ResponseEntity.ok(service.listarDisciplina(pageable)); }

    @GetMapping("/{id}")
    public ResponseEntity<Disciplina> buscar(@PathVariable Long id) { return ResponseEntity.ok(service.buscarDisciplina(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Disciplina> cadastrar(@Valid @RequestBody DadosDisciplina dados) {
        Disciplina criado = service.salvarDisciplina(null, dados);
        return ResponseEntity.created(URI.create("/disciplinas/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Disciplina> atualizar(@PathVariable Long id, @Valid @RequestBody DadosDisciplina dados) { return ResponseEntity.ok(service.salvarDisciplina(id, dados)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) { service.inativarDisciplina(id); return ResponseEntity.noContent().build(); }
}
