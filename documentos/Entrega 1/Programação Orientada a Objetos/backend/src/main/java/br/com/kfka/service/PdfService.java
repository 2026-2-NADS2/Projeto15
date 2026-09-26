package br.com.kfka.service;

import br.com.kfka.model.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.*;
import java.util.*;

@Service
public class PdfService {
    private final AcompanhamentoService acompanhamentos;
    private final AcessoService acesso;
    public PdfService(AcompanhamentoService acompanhamentos, AcessoService acesso) { this.acompanhamentos = acompanhamentos; this.acesso = acesso; }

    @Transactional(readOnly=true)
    public byte[] gerar(Long id) {
        if (acesso.usuarioAtual().getPerfil() != PerfilUsuario.ADMINISTRADOR && acesso.usuarioAtual().getPerfil() != PerfilUsuario.RESPONSAVEL) acesso.proibido();
        Acompanhamento a = acompanhamentos.buscarPermitido(id);
        if (a.getStatus() != StatusAcompanhamento.PUBLICADO)
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "PDF disponível somente para acompanhamento publicado");
        try (PDDocument documento = new PDDocument(); InputStream fonteArquivo = new ClassPathResource("fonts/NotoSans-Regular.ttf").getInputStream(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            PDType0Font fonte = PDType0Font.load(documento, fonteArquivo);
            List<String> conteudo = List.of("KFKA - Acompanhamento escolar", "Aluno: " + a.getAluno().getNome(),
                "Turma: " + a.getTurma().getNome() + " | Série: " + a.getTurma().getSerie(),
                "Ano letivo: " + a.getBimestre().getAnoLetivo() + " | Bimestre: " + a.getBimestre().getNumero(),
                "Disciplina: " + a.getDisciplina().getNome(), "Professor: " + a.getProfessor().getUsuario().getNome(),
                "Média: " + a.getMedia().toPlainString(), "Tags: " + String.join(", ", a.getTags().stream().map(Tag::getNome).sorted().toList()),
                "", "Descrição:", a.getDescricao());
            List<String> linhas = new ArrayList<>();
            for (String texto : conteudo) linhas.addAll(quebrar(normalizar(texto, fonte), fonte));
            int porPagina = 40;
            for (int inicio = 0; inicio < linhas.size(); inicio += porPagina) {
                PDPage pagina = new PDPage(PDRectangle.A4); documento.addPage(pagina);
                try (PDPageContentStream stream = new PDPageContentStream(documento, pagina)) {
                    stream.beginText(); stream.setFont(fonte, 12); stream.setLeading(18); stream.newLineAtOffset(50, 790);
                    for (String linha : linhas.subList(inicio, Math.min(inicio + porPagina, linhas.size()))) { stream.setFont(fonte, inicio == 0 && linha.equals(linhas.get(0)) ? 14 : 12); stream.showText(linha); stream.newLine(); }
                    stream.endText();
                    stream.beginText(); stream.setFont(fonte, 9); stream.newLineAtOffset(50, 35);
                    stream.showText("Acompanhamento " + id + " - Publicado - Página " + (inicio / porPagina + 1)); stream.endText();
                }
            }
            documento.getDocumentInformation().setTitle("KFKA - Acompanhamento " + id);
            documento.save(saida); return saida.toByteArray();
        } catch (IOException ex) { throw new IllegalStateException("Não foi possível gerar o PDF", ex); }
    }

    private String normalizar(String texto, PDType0Font fonte) throws IOException {
        StringBuilder resultado = new StringBuilder();
        for (int ponto : texto.codePoints().toArray()) {
            if (ponto == '\n' || ponto == '\r') resultado.append('\n');
            else if (Character.isISOControl(ponto)) resultado.append(' ');
            else {
                String caractere = new String(Character.toChars(ponto));
                try { fonte.encode(caractere); resultado.append(caractere); }
                catch (IllegalArgumentException ex) { resultado.append('?'); }
            }
        }
        return resultado.toString();
    }

    private List<String> quebrar(String texto, PDType0Font fonte) throws IOException {
        List<String> linhas = new ArrayList<>();
        float largura = PDRectangle.A4.getWidth() - 100;
        for (String paragrafo : texto.split("\\n", -1)) {
            StringBuilder linha = new StringBuilder();
            for (int ponto : paragrafo.codePoints().toArray()) {
                String caractere = new String(Character.toChars(ponto));
                if (fonte.getStringWidth(linha + caractere) / 1000 * 12 > largura) {
                    int espaco = linha.lastIndexOf(" ");
                    if (espaco > 0) {
                        linhas.add(linha.substring(0, espaco));
                        linha = new StringBuilder(linha.substring(espaco + 1));
                    } else { linhas.add(linha.toString()); linha.setLength(0); }
                }
                linha.append(caractere);
            }
            linhas.add(linha.toString());
        }
        return linhas;
    }
}
