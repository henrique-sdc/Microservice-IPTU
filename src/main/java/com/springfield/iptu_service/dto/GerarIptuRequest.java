package com.springfield.iptu_service.dto;

import com.springfield.iptu_service.model.TipoPagamento;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GerarIptuRequest {

    @NotNull
    private Integer cidadaoId;

    @NotNull
    private Integer ano;

    @NotNull
    private TipoPagamento tipoPagamento;
}