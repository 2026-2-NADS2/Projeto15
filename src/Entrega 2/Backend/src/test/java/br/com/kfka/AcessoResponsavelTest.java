package br.com.kfka;

import br.com.kfka.model.*;
import br.com.kfka.repository.*;
import br.com.kfka.service.UsuarioService;
import br.com.kfka.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Set;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AcessoResponsavelTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioService usuarios;
    @Autowired AlunoRepository alunos;
    @Autowired ResponsavelRepository responsaveis;
    @Autowired JwtService jwt;

    @Test void responsavelConsultaSomenteSeusAlunos() throws Exception {
        Usuario usuario = usuarios.salvar(null, new UsuarioCadastro("Responsável", "familia@teste.local", "SenhaTeste123!", PerfilUsuario.RESPONSAVEL, true));
        Aluno proprio = new Aluno(); proprio.setNome("Aluno vinculado"); alunos.save(proprio);
        Aluno outro = new Aluno(); outro.setNome("Outro aluno"); alunos.save(outro);
        Responsavel r = new Responsavel(); r.setUsuario(usuario); r.setAlunos(Set.of(proprio)); responsaveis.saveAndFlush(r);
        String token = "Bearer " + jwt.generateToken(usuario);
        mvc.perform(get("/alunos").header("Authorization", token)).andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].id").value(proprio.getId()));
        mvc.perform(get("/alunos/" + outro.getId()).header("Authorization", token)).andExpect(status().isNotFound());
        mvc.perform(get("/alunos/" + outro.getId() + "/acompanhamentos").header("Authorization", token)).andExpect(status().isNotFound());
        mvc.perform(get("/usuarios").header("Authorization", token)).andExpect(status().isForbidden());
    }
}
