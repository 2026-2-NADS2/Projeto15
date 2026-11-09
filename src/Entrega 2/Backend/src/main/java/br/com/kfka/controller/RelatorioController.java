package br.com.kfka.controller;

import br.com.kfka.model.*;
import br.com.kfka.service.RelatorioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.tags.Tag(name="Relatorio")
@RestController
@RequestMapping("/relatorios")
public class RelatorioController {
    private final RelatorioService service;
    public RelatorioController(RelatorioService service) { this.service = service; }
    @GetMapping
    public ResponseEntity<Page<AcompanhamentoResposta>> consultar(@Valid @ModelAttribute FiltroRelatorio filtro, Pageable pageable) {
        return ResponseEntity.ok(service.consultar(filtro, pageable));
    }
    @GetMapping("/indicadores")
    public ResponseEntity<IndicadoresResposta> indicadores() { return ResponseEntity.ok(service.indicadores()); }
    @GetMapping(value="/exportar/csv", produces="text/csv;charset=UTF-8")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<byte[]> exportar(@Valid @ModelAttribute FiltroRelatorio filtro) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-kfka.csv")
            .cacheControl(CacheControl.noStore()).contentType(MediaType.parseMediaType("text/csv;charset=UTF-8")).body(service.exportarCsv(filtro));
    }
    @GetMapping(value="/exportar/xlsx", produces="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<byte[]> exportarExcel(@Valid @ModelAttribute FiltroRelatorio filtro) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-kfka.xlsx")
            .cacheControl(CacheControl.noStore())
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(service.exportarXlsx(filtro));
    }
}
