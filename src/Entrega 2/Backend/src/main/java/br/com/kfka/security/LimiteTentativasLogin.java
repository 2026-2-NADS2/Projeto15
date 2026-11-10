package br.com.kfka.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Protege o login contra tentativas repetidas de senha (força bruta).
 * Depois de MAXIMO_FALHAS erros seguidos para o mesmo e-mail dentro da janela,
 * o e-mail fica bloqueado por DURACAO_BLOQUEIO. Um login correto zera o contador.
 */
@Component
public class LimiteTentativasLogin {
    static final int MAXIMO_FALHAS = 5;
    static final Duration JANELA = Duration.ofMinutes(15);
    static final Duration DURACAO_BLOQUEIO = Duration.ofMinutes(15);
    private static final int LIMITE_REGISTROS = 10_000;

    private final Clock clock;
    private final Map<String, Registro> registros = new ConcurrentHashMap<>();

    public LimiteTentativasLogin(Clock clock) {
        this.clock = clock;
    }

    /** Minutos restantes de bloqueio para o e-mail, ou 0 se pode tentar. */
    public long minutosBloqueado(String email) {
        Registro r = registros.get(email);
        if (r == null || r.bloqueadoAte == null) return 0;
        Duration restante = Duration.between(clock.instant(), r.bloqueadoAte);
        if (restante.isNegative() || restante.isZero()) {
            registros.remove(email);
            return 0;
        }
        return Math.max(1, (restante.toSeconds() + 59) / 60);
    }

    public void registrarFalha(String email) {
        Instant agora = clock.instant();
        if (registros.size() > LIMITE_REGISTROS) limparExpirados(agora);
        registros.compute(email, (chave, atual) -> {
            Registro r = (atual == null || atual.inicio.plus(JANELA).isBefore(agora)) ? new Registro(agora) : atual;
            r.falhas++;
            if (r.falhas >= MAXIMO_FALHAS) r.bloqueadoAte = agora.plus(DURACAO_BLOQUEIO);
            return r;
        });
    }

    public void registrarSucesso(String email) {
        registros.remove(email);
    }

    private void limparExpirados(Instant agora) {
        registros.entrySet().removeIf(e -> e.getValue().inicio.plus(JANELA).isBefore(agora)
            && (e.getValue().bloqueadoAte == null || e.getValue().bloqueadoAte.isBefore(agora)));
    }

    private static class Registro {
        private final Instant inicio;
        private int falhas;
        private Instant bloqueadoAte;

        Registro(Instant inicio) { this.inicio = inicio; }
    }
}
