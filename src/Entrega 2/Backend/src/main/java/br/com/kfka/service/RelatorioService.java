package br.com.kfka.service;

import br.com.kfka.model.*;
import br.com.kfka.repository.AcompanhamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.*;
import jakarta.persistence.criteria.Predicate;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.math.*;
import java.util.stream.Collectors;
import java.io.ByteArrayOutputStream;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
@Transactional(readOnly=true)
public class RelatorioService {
    private final AcompanhamentoRepository repository;
    private final AcessoService acesso;
    public RelatorioService(AcompanhamentoRepository repository, AcessoService acesso) { this.repository = repository; this.acesso = acesso; }

    private Specification<Acompanhamento> filtros(FiltroRelatorio f) {
        return (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (f.turmaId() != null) condicoes.add(cb.equal(root.get("turma").get("id"), f.turmaId()));
            if (f.disciplinaId() != null) condicoes.add(cb.equal(root.get("disciplina").get("id"), f.disciplinaId()));
            if (f.professorId() != null) condicoes.add(cb.equal(root.get("professor").get("id"), f.professorId()));
            if (f.bimestreId() != null) condicoes.add(cb.equal(root.get("bimestre").get("id"), f.bimestreId()));
            if (f.alunoId() != null) condicoes.add(cb.equal(root.get("aluno").get("id"), f.alunoId()));
            if (f.tagId() != null) condicoes.add(cb.equal(root.join("tags").get("id"), f.tagId()));
            if (f.anoLetivo() != null) condicoes.add(cb.equal(root.get("bimestre").get("anoLetivo"), f.anoLetivo()));
            if (f.bimestre() != null) condicoes.add(cb.equal(root.get("bimestre").get("numero"), f.bimestre()));
            if (f.status() != null) condicoes.add(cb.equal(root.get("status"), f.status()));
            return cb.and(condicoes.toArray(Predicate[]::new));
        };
    }

    public Page<AcompanhamentoResposta> consultar(FiltroRelatorio filtro, Pageable pageable) {
        return repository.findAll(acesso.acompanhamentos().and(filtros(filtro)), pageable).map(AcompanhamentoResposta::de);
    }

    public byte[] exportarCsv(FiltroRelatorio filtro) {
        acesso.exigirPerfil(PerfilUsuario.ADMINISTRADOR);
        StringBuilder csv = new StringBuilder("\uFEFFid;aluno;turma;ano_letivo;bimestre;disciplina;professor;descricao;media;tags;status\r\n");
        Pageable pagina = PageRequest.of(0, 200, Sort.by("id"));
        Page<AcompanhamentoResposta> resultado;
        do {
            resultado = consultar(filtro, pagina);
            for (AcompanhamentoResposta a : resultado.getContent()) {
                List<String> valores = List.of(a.id().toString(), a.aluno().getNome(), a.turma().getNome(),
                    a.bimestre().getAnoLetivo().toString(), a.bimestre().getNumero().toString(), a.disciplina().getNome(),
                    a.professor().nome(), a.descricao(), a.media().toPlainString(), String.join(", ", a.tags().stream().map(Tag::getNome).sorted().toList()), a.status().name());
                csv.append(String.join(";", valores.stream().map(this::escapar).toList())).append("\r\n");
            }
            pagina = resultado.nextPageable();
        } while (resultado.hasNext());
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportarXlsx(FiltroRelatorio filtro) {
        acesso.exigirPerfil(PerfilUsuario.ADMINISTRADOR);
        List<AcompanhamentoResposta> registros = repository.findAll(acesso.acompanhamentos().and(filtros(filtro)), Sort.by("id"))
            .stream().map(AcompanhamentoResposta::de).toList();
        try (XSSFWorkbook arquivo = new XSSFWorkbook(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            var planilha = arquivo.createSheet("Acompanhamentos");
            String[] colunas = {"ID", "Aluno", "Turma", "Ano letivo", "Bimestre", "Disciplina", "Professor", "Descrição", "Média", "Tags", "Estado"};
            var cabecalho = planilha.createRow(0);
            for (int i = 0; i < colunas.length; i++) cabecalho.createCell(i).setCellValue(colunas[i]);
            int linha = 1;
            for (AcompanhamentoResposta a : registros) {
                var row = planilha.createRow(linha++);
                row.createCell(0).setCellValue(a.id());
                row.createCell(1).setCellValue(a.aluno().getNome());
                row.createCell(2).setCellValue(a.turma().getNome());
                row.createCell(3).setCellValue(a.bimestre().getAnoLetivo());
                row.createCell(4).setCellValue(a.bimestre().getNumero());
                row.createCell(5).setCellValue(a.disciplina().getNome());
                row.createCell(6).setCellValue(a.professor().nome());
                row.createCell(7).setCellValue(a.descricao());
                row.createCell(8).setCellValue(a.media().doubleValue());
                row.createCell(9).setCellValue(String.join(", ", a.tags().stream().map(Tag::getNome).sorted().toList()));
                row.createCell(10).setCellValue(a.status().name());
            }
            for (int i = 0; i < colunas.length; i++) planilha.autoSizeColumn(i);
            arquivo.write(saida);
            return saida.toByteArray();
        } catch (java.io.IOException erro) {
            throw new IllegalStateException("Não foi possível gerar a planilha", erro);
        }
    }

    public IndicadoresResposta indicadores() {
        List<Acompanhamento> registros = repository.findAll(acesso.acompanhamentos());
        long publicados = registros.stream().filter(a -> a.getStatus() == StatusAcompanhamento.PUBLICADO).count();
        BigDecimal media = registros.isEmpty() ? BigDecimal.ZERO : registros.stream()
            .map(Acompanhamento::getMedia).reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(registros.size()), 2, RoundingMode.HALF_UP);
        Map<Disciplina, List<Acompanhamento>> grupos = registros.stream()
            .collect(Collectors.groupingBy(Acompanhamento::getDisciplina));
        List<IndicadoresResposta.MediaDisciplina> disciplinas = grupos.entrySet().stream()
            .map(entry -> new IndicadoresResposta.MediaDisciplina(entry.getKey().getId(), entry.getKey().getNome(),
                entry.getValue().stream().map(Acompanhamento::getMedia).reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(entry.getValue().size()), 2, RoundingMode.HALF_UP)))
            .sorted(Comparator.comparing(IndicadoresResposta.MediaDisciplina::disciplina)).toList();
        return new IndicadoresResposta(registros.size(), publicados, media, disciplinas);
    }

    private String escapar(String valor) {
        String inicio = valor.stripLeading();
        if (!inicio.isEmpty() && "=+-@".indexOf(inicio.charAt(0)) >= 0) valor = "'" + valor;
        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }
}
