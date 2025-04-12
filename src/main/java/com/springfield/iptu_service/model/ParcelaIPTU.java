package com.springfield.iptu_service.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PARCELA_IPTU")
@Data
public class ParcelaIPTU {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "iptu_anual_id", nullable = false)
    @JsonBackReference
    private IptuAnual iptuAnual;

    @Column(name = "numero_parcela", nullable = false)
    private Integer numeroParcela;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(nullable = false)
    private boolean pago = false;

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;
}