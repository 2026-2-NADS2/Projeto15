package br.com.kfka.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AnoLetivo(
    @NotNull @Min(2000) @Max(2100) Integer ano,
    long totalBimestres
) {}
