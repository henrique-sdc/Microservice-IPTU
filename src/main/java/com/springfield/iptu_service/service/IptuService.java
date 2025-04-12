package com.springfield.iptu_service.service;

import com.springfield.iptu_service.client.CidadaoClient;
import com.springfield.iptu_service.dto.SituacaoIptuResponse;
import com.springfield.iptu_service.model.IptuAnual;
import com.springfield.iptu_service.model.ParcelaIPTU;
import com.springfield.iptu_service.model.TipoPagamento;
import com.springfield.iptu_service.repository.IptuAnualRepository;
import com.springfield.iptu_service.repository.ParcelaIPTURepository;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
public class IptuService {

    private final IptuAnualRepository iptuAnualRepository;
    private final ParcelaIPTURepository parcelaIPTURepository;
    private final CidadaoClient cidadaoClient;

    private static final BigDecimal VALOR_TOTAL_ANUAL = new BigDecimal("12000.00");
    private static final BigDecimal VALOR_PARCELA_MENSAL = new BigDecimal("1000.00");
    private static final BigDecimal VALOR_PRIMEIRA_PARCELA_DESCONTO = new BigDecimal("1000.00");
    private static final BigDecimal VALOR_PARCELA_ZERADA = BigDecimal.ZERO;
    private static final int NUMERO_PARCELAS = 12;

    public IptuService(IptuAnualRepository iptuAnualRepository,
            ParcelaIPTURepository parcelaIPTURepository,
            CidadaoClient cidadaoClient) {
        this.iptuAnualRepository = iptuAnualRepository;
        this.parcelaIPTURepository = parcelaIPTURepository;
        this.cidadaoClient = cidadaoClient;
    }

    @Transactional
    public IptuAnual gerarIptu(Integer cidadaoId, Integer ano, TipoPagamento tipoPagamento) {

        try {
            cidadaoClient.verificarCidadaoExiste(cidadaoId);
        } catch (FeignException e) {
            if (e.status() == HttpStatus.NOT_FOUND.value()) {
                throw new RuntimeException("Cidadão com ID " + cidadaoId + " não encontrado no serviço de cidadãos.");
            } else {
                throw new RuntimeException("Erro ao verificar cidadão: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro inesperado ao verificar cidadão: " + e.getMessage(), e);
        }

        if (iptuAnualRepository.existsByCidadaoIdAndAno(cidadaoId, ano)) {
            throw new RuntimeException("IPTU para o cidadão " + cidadaoId + " no ano " + ano + " já foi gerado.");
        }

        IptuAnual iptuAnual = new IptuAnual();
        iptuAnual.setCidadaoId(cidadaoId);
        iptuAnual.setAno(ano);
        iptuAnual.setTipoPagamento(tipoPagamento);
        iptuAnual.setValorTotal(VALOR_TOTAL_ANUAL);

        List<ParcelaIPTU> parcelas = new ArrayList<>();
        for (int mes = 1; mes <= NUMERO_PARCELAS; mes++) {
            ParcelaIPTU parcela = new ParcelaIPTU();
            parcela.setIptuAnual(iptuAnual);
            parcela.setNumeroParcela(mes);
            parcela.setPago(false);

            if (tipoPagamento == TipoPagamento.UNICO) {
                parcela.setValor(mes == 1 ? VALOR_PRIMEIRA_PARCELA_DESCONTO : VALOR_PARCELA_ZERADA);
            } else {
                parcela.setValor(VALOR_PARCELA_MENSAL);
            }

            LocalDate vencimento = LocalDate.of(ano, mes, 1).with(TemporalAdjusters.lastDayOfMonth());
            parcela.setDataVencimento(vencimento);

            parcelas.add(parcela);
        }

        iptuAnual.setParcelas(parcelas);

        return iptuAnualRepository.save(iptuAnual);
    }

    public SituacaoIptuResponse consultarSituacaoIptu(Integer cidadaoId, Integer ano) {
        IptuAnual iptuAnual = iptuAnualRepository.findByCidadaoIdAndAno(cidadaoId, ano)
                .orElseThrow(() -> new RuntimeException(
                        "IPTU não encontrado para o cidadão " + cidadaoId + " no ano " + ano));

        List<ParcelaIPTU> parcelas = parcelaIPTURepository.findByIptuAnualIdOrderByNumeroParcelaAsc(iptuAnual.getId());

        BigDecimal totalPago = parcelaIPTURepository.sumValorPagoByIptuAnualId(iptuAnual.getId())
                .orElse(BigDecimal.ZERO);
        BigDecimal totalDevido = parcelaIPTURepository.sumValorDevidoByIptuAnualId(iptuAnual.getId())
                .orElse(BigDecimal.ZERO);

        long countParcelasPagas = parcelas.stream()
                .filter(ParcelaIPTU::isPago)
                .count();

        return new SituacaoIptuResponse(
                iptuAnual.getId(),
                iptuAnual.getCidadaoId(),
                iptuAnual.getAno(),
                iptuAnual.getTipoPagamento().name(),
                totalPago,
                totalDevido,
                countParcelasPagas,
                parcelas);
    }

    @Transactional
    public ParcelaIPTU baixarParcela(Integer cidadaoId, Integer ano, Integer numeroParcela) {
        IptuAnual iptuAnual = iptuAnualRepository.findByCidadaoIdAndAno(cidadaoId, ano)
                .orElseThrow(() -> new RuntimeException(
                        "IPTU não encontrado para o cidadão " + cidadaoId + " no ano " + ano));

        ParcelaIPTU parcela = parcelaIPTURepository.findByIptuAnualIdAndNumeroParcela(iptuAnual.getId(), numeroParcela)
                .orElseThrow(() -> new RuntimeException(
                        "Parcela " + numeroParcela + " não encontrada para o IPTU do ano " + ano));

        if (parcela.isPago()) {
            throw new RuntimeException("Parcela " + numeroParcela + " já consta como paga.");
        }

        parcela.setPago(true);
        parcela.setDataPagamento(LocalDateTime.now());

        return parcelaIPTURepository.save(parcela);
    }
}