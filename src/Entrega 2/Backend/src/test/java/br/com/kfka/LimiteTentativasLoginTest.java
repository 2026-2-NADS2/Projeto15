package br.com.kfka;

import static org.junit.jupiter.api.Assertions.*;

import br.com.kfka.security.LimiteTentativasLogin;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class LimiteTentativasLoginTest {
    /** Relógio que o teste consegue adiantar. */
    static class RelogioManual extends Clock {
        Instant agora = Instant.parse("2026-11-09T12:00:00Z");
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return agora; }
        void avancar(Duration d) { agora = agora.plus(d); }
    }

    @Test
    void bloqueiaDepoisDeCincoFalhasEPassaDepoisDoPrazo() {
        RelogioManual relogio = new RelogioManual();
        LimiteTentativasLogin limite = new LimiteTentativasLogin(relogio);
        for (int i = 0; i < 4; i++) limite.registrarFalha("a@kfka.com");
        assertEquals(0, limite.minutosBloqueado("a@kfka.com"));
        limite.registrarFalha("a@kfka.com");
        assertEquals(15, limite.minutosBloqueado("a@kfka.com"));
        assertEquals(0, limite.minutosBloqueado("outro@kfka.com"));
        relogio.avancar(Duration.ofMinutes(14));
        assertEquals(1, limite.minutosBloqueado("a@kfka.com"));
        relogio.avancar(Duration.ofMinutes(1));
        assertEquals(0, limite.minutosBloqueado("a@kfka.com"));
    }

    @Test
    void sucessoZeraOContador() {
        LimiteTentativasLogin limite = new LimiteTentativasLogin(new RelogioManual());
        for (int i = 0; i < 4; i++) limite.registrarFalha("b@kfka.com");
        limite.registrarSucesso("b@kfka.com");
        for (int i = 0; i < 4; i++) limite.registrarFalha("b@kfka.com");
        assertEquals(0, limite.minutosBloqueado("b@kfka.com"));
    }

    @Test
    void falhasAntigasForaDaJanelaNaoSomam() {
        RelogioManual relogio = new RelogioManual();
        LimiteTentativasLogin limite = new LimiteTentativasLogin(relogio);
        for (int i = 0; i < 4; i++) limite.registrarFalha("c@kfka.com");
        relogio.avancar(Duration.ofMinutes(16));
        limite.registrarFalha("c@kfka.com");
        assertEquals(0, limite.minutosBloqueado("c@kfka.com"));
    }
}
