package br.com.kfka;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProjetoTest {
    @Test
    void possuiClassePrincipal() {
        assertNotNull(KfkaApplication.class.getAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }
}
