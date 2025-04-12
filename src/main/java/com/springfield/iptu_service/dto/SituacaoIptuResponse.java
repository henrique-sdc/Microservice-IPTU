package com.springfield.iptu_service.dto;

import com.springfield.iptu_service.model.ParcelaIPTU;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SituacaoIptuResponse {
    private Integer iptuAnualId;
    private Integer cidadaoId;
    private Integer ano;
    private String tipoPagamento;
    private BigDecimal totalPago;
    private BigDecimal totalDevido;
    private Long totalParcelasPagas;
    private List<ParcelaIPTU> parcelas;
}