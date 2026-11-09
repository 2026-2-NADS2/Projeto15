package br.com.kfka.model;

import java.math.BigDecimal;
import java.util.List;

public record IndicadoresResposta(long acompanhamentos, long publicados, BigDecimal mediaGeral,
    List<MediaDisciplina> mediasPorDisciplina) {
    public record MediaDisciplina(Long disciplinaId, String disciplina, BigDecimal media) {}
}
