package br.com.kfka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.persistence.EntityManagerFactory;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties={
    "spring.datasource.url=jdbc:h2:mem:schema_prod;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=file:sql/01_schema.sql,file:sql/02_view_relatorios.sql",
    "kfka.cors.origens=http://localhost:5173"
})
@AutoConfigureMockMvc
@ActiveProfiles({"test", "prod"})
class SchemaProducaoTest {
    @Autowired EntityManagerFactory factory;
    @Autowired JdbcTemplate banco;
    @Autowired MockMvc mvc;

    @Test void validaSchemaInicialEViewComConfiguracaoDeProducao() throws Exception {
        assertTrue(factory.isOpen());
        assertEquals(0L, banco.queryForObject("select count(*) from vw_acompanhamentos_publicados", Long.class));
        mvc.perform(get("/status")).andExpect(status().isOk());
        mvc.perform(get("/alunos")).andExpect(status().isUnauthorized());
    }
}
