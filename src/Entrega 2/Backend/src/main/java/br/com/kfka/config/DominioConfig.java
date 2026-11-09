package br.com.kfka.config;

import java.time.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class DominioConfig {
    @Bean public Clock clock(@Value("${kfka.fuso-horario}") String fuso) { return Clock.system(ZoneId.of(fuso)); }
}
