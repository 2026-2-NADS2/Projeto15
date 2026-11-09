package br.com.kfka.model;

import jakarta.validation.constraints.Size;

public record CienciaCadastro(@Size(max=1000) String observacao) {}
