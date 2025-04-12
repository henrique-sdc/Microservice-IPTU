package com.springfield.iptu_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "IPTU_ANUAL")
@Data
public class IptuAnual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cidadao_id", nullable = false)
    private Integer cidadaoId;

    @Column(nullable = false)
    private Integer ano;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal = new BigDecimal("12000.00");

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pagamento", nullable = false, length = 10)
    private TipoPagamento tipoPagamento;

    @Column(name = "data_geracao", nullable = false)
    private LocalDateTime dataGeracao;

    @OneToMany(mappedBy = "iptuAnual", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ParcelaIPTU> parcelas;

    @PrePersist
    protected void onCreate() {
        dataGeracao = LocalDateTime.now();
    }
}