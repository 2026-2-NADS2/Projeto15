package br.com.kfka.controller;

import br.com.kfka.model.AreaDisciplina;
import br.com.kfka.service.CadastroService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;

@io.swagger.v3.oas.annotations.tags.Tag(name="AreaDisciplina")
@RestController
@RequestMapping("/areas")
public class AreaDisciplinaController {
    private final CadastroService service;
    public AreaDisciplinaController(CadastroService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<Page<AreaDisciplina>> listar(Pageable pageable) { return ResponseEntity.ok(service.listarAreaDisciplina(pageable)); }

    @GetMapping("/{id}")
    public ResponseEntity<AreaDisciplina> buscar(@PathVariable Long id) { return ResponseEntity.ok(service.buscarAreaDisciplina(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AreaDisciplina> cadastrar(@Valid @RequestBody AreaDisciplina dados) {
        AreaDisciplina criado = service.salvarAreaDisciplina(null, dados);
        return ResponseEntity.created(URI.create("/areas/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AreaDisciplina> atualizar(@PathVariable Long id, @Valid @RequestBody AreaDisciplina dados) {
        return ResponseEntity.ok(service.salvarAreaDisciplina(id, dados));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) { service.inativarAreaDisciplina(id); return ResponseEntity.noContent().build(); }
}
