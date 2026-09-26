package br.com.kfka.controller;

import br.com.kfka.model.Tag;
import br.com.kfka.service.CadastroService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.net.URI;

@io.swagger.v3.oas.annotations.tags.Tag(name="Tag")
@RestController
@RequestMapping("/tags")
public class TagController {
    private final CadastroService service;
    public TagController(CadastroService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<Page<Tag>> listar(Pageable pageable) { return ResponseEntity.ok(service.listarTag(pageable)); }

    @GetMapping("/{id}")
    public ResponseEntity<Tag> buscar(@PathVariable Long id) { return ResponseEntity.ok(service.buscarTag(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Tag> cadastrar(@Valid @RequestBody Tag dados) {
        Tag criado = service.salvarTag(null, dados);
        return ResponseEntity.created(URI.create("/tags/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Tag> atualizar(@PathVariable Long id, @Valid @RequestBody Tag dados) {
        return ResponseEntity.ok(service.salvarTag(id, dados));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) { service.inativarTag(id); return ResponseEntity.noContent().build(); }
}
