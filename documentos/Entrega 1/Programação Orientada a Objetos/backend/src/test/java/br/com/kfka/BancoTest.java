package br.com.kfka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BancoTest {
    @org.springframework.beans.factory.annotation.Autowired jakarta.persistence.EntityManagerFactory factory;
    @Test void iniciaComTodosOsRelacionamentosERepositories() {
        org.junit.jupiter.api.Assertions.assertTrue(factory.isOpen());
        org.junit.jupiter.api.Assertions.assertEquals(16, factory.getMetamodel().getEntities().size());
    }
}
