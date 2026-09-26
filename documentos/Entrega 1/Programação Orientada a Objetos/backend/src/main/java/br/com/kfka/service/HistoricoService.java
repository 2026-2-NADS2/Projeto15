package br.com.kfka.service;

import br.com.kfka.model.*;
import br.com.kfka.repository.HistoricoAcompanhamentoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class HistoricoService {
    private final HistoricoAcompanhamentoRepository repository;
    private final AcessoService acesso;
    private final ObjectMapper mapper;
    private final Clock clock;
    public HistoricoService(HistoricoAcompanhamentoRepository repository, AcessoService acesso, ObjectMapper mapper, Clock clock) {
        this.repository = repository; this.acesso = acesso; this.mapper = mapper; this.clock = clock;
    }
    public String retrato(Acompanhamento a) {
        try {
            return mapper.writeValueAsString(Map.of("descricao", a.getDescricao(), "media", a.getMedia(), "tags",
                a.getTags().stream().sorted(Comparator.comparing(Tag::getId)).map(t -> Map.of("id", t.getId(), "nome", t.getNome())).toList()));
        } catch (JsonProcessingException ex) { throw new IllegalStateException("Não foi possível registrar auditoria", ex); }
    }
    public void registrar(Acompanhamento a, StatusAcompanhamento anterior, String acao, String dadosAnteriores) {
        HistoricoAcompanhamento h = new HistoricoAcompanhamento();
        h.setAcompanhamento(a);
        h.setUsuarioResponsavelPelaAcao(acesso.usuarioAtual());
        h.setDataHora(LocalDateTime.now(clock));
        h.setStatusAnterior(anterior);
        h.setStatusNovo(a.getStatus());
        h.setAcao(acao);
        h.setDadosAnteriores(dadosAnteriores);
        h.setDadosNovos(retrato(a));
        repository.save(h);
    }
    public List<HistoricoResposta> listar(Long id) {
        return repository.findAllByAcompanhamentoIdOrderByDataHoraAscIdAsc(id).stream().map(HistoricoResposta::de).toList();
    }
}
