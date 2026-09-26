package br.com.kfka.security;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter filtro, AuditoriaFilter auditoria, ErroSeguranca erro) throws Exception {
        return http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(f -> f.disable()).httpBasic(b -> b.disable())
            .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> erro.responder(req, res, HttpStatus.UNAUTHORIZED, "Autenticação necessária"))
                .accessDeniedHandler((req, res, ex) -> erro.responder(req, res, HttpStatus.FORBIDDEN, "Acesso proibido")))
            .authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.POST, "/usuarios/logar", "/usuarios/recuperacao-senha", "/usuarios/redefinir-senha").permitAll()
                .requestMatchers(HttpMethod.GET, "/status", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/alunos", "/alunos/**", "/turmas", "/turmas/**").hasAnyRole("ADMINISTRADOR", "PROFESSOR", "RESPONSAVEL")
                .requestMatchers(HttpMethod.GET, "/responsaveis", "/responsaveis/**").hasAnyRole("ADMINISTRADOR", "RESPONSAVEL")
                .requestMatchers(HttpMethod.GET, "/professores", "/professores/**", "/disciplinas", "/disciplinas/**", "/vinculos", "/vinculos/**", "/matriculas", "/matriculas/**", "/areas", "/areas/**", "/tags", "/tags/**", "/bimestres", "/bimestres/**").hasAnyRole("ADMINISTRADOR", "PROFESSOR")
                .requestMatchers("/relatorios/exportar/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/anos-letivos", "/anos-letivos/**", "/auditoria", "/auditoria/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/acompanhamentos", "/acompanhamentos/**", "/relatorios", "/relatorios/**").hasAnyRole("ADMINISTRADOR", "PROFESSOR", "RESPONSAVEL")
                .requestMatchers("/usuarios", "/usuarios/**", "/alunos", "/alunos/**", "/professores", "/professores/**", "/responsaveis", "/responsaveis/**", "/areas", "/areas/**", "/disciplinas", "/disciplinas/**", "/turmas", "/turmas/**", "/matriculas", "/matriculas/**", "/vinculos", "/vinculos/**", "/tags", "/tags/**", "/bimestres", "/bimestres/**").hasRole("ADMINISTRADOR")
                .anyRequest().denyAll())
            .addFilterBefore(filtro, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(auditoria, JwtAuthFilter.class).build();
    }

    @Value("${kfka.cors.origens}") private String origens;
    @Bean public CorsConfigurationSource corsConfigurationSource() {
        List<String> permitidas = Arrays.stream(origens.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        if (permitidas.stream().anyMatch(s -> s.contains("*"))) throw new IllegalArgumentException("CORS_ORIGINS deve conter origens explícitas");
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(permitidas);
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setExposedHeaders(List.of("Content-Disposition"));
        cors.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean public FilterRegistrationBean<JwtAuthFilter> desativarRegistroDuplicado(JwtAuthFilter filtro) {
        FilterRegistrationBean<JwtAuthFilter> registro = new FilterRegistrationBean<>(filtro);
        registro.setEnabled(false);
        return registro;
    }
    @Bean public FilterRegistrationBean<AuditoriaFilter> desativarAuditoriaDuplicada(AuditoriaFilter filtro) {
        FilterRegistrationBean<AuditoriaFilter> registro = new FilterRegistrationBean<>(filtro);
        registro.setEnabled(false);
        return registro;
    }
}
