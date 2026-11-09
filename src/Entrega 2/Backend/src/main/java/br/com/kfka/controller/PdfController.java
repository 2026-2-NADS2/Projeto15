package br.com.kfka.controller;

import br.com.kfka.service.PdfService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.tags.Tag(name="Pdf")
@RestController
@RequestMapping("/acompanhamentos")
public class PdfController {
    private final PdfService service;
    public PdfController(PdfService service) { this.service = service; }
    @GetMapping(value="/{id}/pdf", produces=MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RESPONSAVEL')")
    public ResponseEntity<byte[]> gerar(@PathVariable Long id) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=acompanhamento-" + id + ".pdf")
            .cacheControl(CacheControl.noStore()).contentType(MediaType.APPLICATION_PDF).body(service.gerar(id));
    }
}
