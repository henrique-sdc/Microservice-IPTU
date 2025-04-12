package com.springfield.iptu_service.service;

import com.springfield.iptu_service.client.CidadaoClient;
import com.springfield.iptu_service.client.IptuApiTestClient;
import com.springfield.iptu_service.dto.GerarIptuRequest;
import com.springfield.iptu_service.dto.SituacaoIptuResponse;
import com.springfield.iptu_service.model.IptuAnual;
import com.springfield.iptu_service.model.ParcelaIPTU;
import com.springfield.iptu_service.model.TipoPagamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableFeignClients(clients = IptuApiTestClient.class)
@ActiveProfiles("test")
public class IptuControllerFeignTest {

    @LocalServerPort
    private int port;

    @Autowired
    private IptuApiTestClient iptuApiTestClient;

    private CidadaoClient cidadaoClient;

    private final Integer CIDADAO_ID_TESTE = 9999;
    private final Integer ANO_TESTE = LocalDate.now().getYear();

    @BeforeEach
    void setUp() {
        Mockito.when(cidadaoClient.verificarCidadaoExiste(anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        Mockito.when(cidadaoClient.verificarCidadaoExiste(Mockito.eq(CIDADAO_ID_TESTE + 1)))
                .thenReturn(ResponseEntity.notFound().build());
    }

    @Test
    void deveGerarIptuParceladoComSucesso() {
        GerarIptuRequest request = new GerarIptuRequest();
        request.setCidadaoId(CIDADAO_ID_TESTE);
        request.setAno(ANO_TESTE);
        request.setTipoPagamento(TipoPagamento.PARCELADO);

        ResponseEntity<IptuAnual> response = iptuApiTestClient.gerarIptu(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(CIDADAO_ID_TESTE, response.getBody().getCidadaoId());
        assertEquals(ANO_TESTE, response.getBody().getAno());
        assertEquals(TipoPagamento.PARCELADO, response.getBody().getTipoPagamento());
        assertNotNull(response.getBody().getParcelas());
        assertEquals(12, response.getBody().getParcelas().size());

        assertEquals(0, new BigDecimal("1000.00").compareTo(response.getBody().getParcelas().get(0).getValor()));
        assertEquals(0, new BigDecimal("1000.00").compareTo(response.getBody().getParcelas().get(11).getValor()));
    }

    @Test
    void deveGerarIptuUnicoComSucesso() {
        GerarIptuRequest request = new GerarIptuRequest();
        request.setCidadaoId(CIDADAO_ID_TESTE);
        request.setAno(ANO_TESTE + 1);
        request.setTipoPagamento(TipoPagamento.UNICO);

        ResponseEntity<IptuAnual> response = iptuApiTestClient.gerarIptu(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TipoPagamento.UNICO, response.getBody().getTipoPagamento());
        assertEquals(12, response.getBody().getParcelas().size());
        assertEquals(0, new BigDecimal("1000.00").compareTo(response.getBody().getParcelas().get(0).getValor()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getBody().getParcelas().get(1).getValor()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getBody().getParcelas().get(11).getValor()));
    }

    @Test
    void deveConsultarSituacaoIptu() {
        GerarIptuRequest request = new GerarIptuRequest();
        request.setCidadaoId(CIDADAO_ID_TESTE);
        request.setAno(ANO_TESTE);
        request.setTipoPagamento(TipoPagamento.PARCELADO);
        iptuApiTestClient.gerarIptu(request);

        ResponseEntity<SituacaoIptuResponse> response = iptuApiTestClient.consultarSituacao(CIDADAO_ID_TESTE,
                ANO_TESTE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(CIDADAO_ID_TESTE, response.getBody().getCidadaoId());
        assertEquals(ANO_TESTE, response.getBody().getAno());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getBody().getTotalPago()));
        assertEquals(0, new BigDecimal("12000.00").compareTo(response.getBody().getTotalDevido()));
        assertEquals(12, response.getBody().getParcelas().size());
    }

    @Test
    void deveBaixarParcelaComSucesso() {
        GerarIptuRequest request = new GerarIptuRequest();
        request.setCidadaoId(CIDADAO_ID_TESTE);
        request.setAno(ANO_TESTE);
        request.setTipoPagamento(TipoPagamento.PARCELADO);
        iptuApiTestClient.gerarIptu(request);

        int numeroParcela = 1;
        ResponseEntity<Map<String, Object>> responseBaixa = iptuApiTestClient.baixarParcela(CIDADAO_ID_TESTE, ANO_TESTE,
                numeroParcela);

        assertEquals(HttpStatus.OK, responseBaixa.getStatusCode());
        assertNotNull(responseBaixa.getBody());
        assertTrue(responseBaixa.getBody().containsKey("mensagem"));
        assertTrue(responseBaixa.getBody().containsKey("parcela"));

        @SuppressWarnings("unchecked")
        Map<String, Object> parcelaMap = (Map<String, Object>) responseBaixa.getBody().get("parcela");
        assertNotNull(parcelaMap);
        assertEquals(numeroParcela, parcelaMap.get("numeroParcela"));
        assertTrue((Boolean) parcelaMap.get("pago"));
        assertNotNull(parcelaMap.get("dataPagamento"));

        ResponseEntity<SituacaoIptuResponse> responseSituacao = iptuApiTestClient.consultarSituacao(CIDADAO_ID_TESTE,
                ANO_TESTE);
        assertEquals(HttpStatus.OK, responseSituacao.getStatusCode());
        assertNotNull(responseSituacao.getBody());
        assertEquals(0, new BigDecimal("1000.00").compareTo(responseSituacao.getBody().getTotalPago()));
        assertEquals(0, new BigDecimal("11000.00").compareTo(responseSituacao.getBody().getTotalDevido()));

        ParcelaIPTU parcelaConsultada = responseSituacao.getBody().getParcelas().stream()
                .filter(p -> p.getNumeroParcela() == numeroParcela)
                .findFirst()
                .orElse(null);

        assertNotNull(parcelaConsultada);
        assertTrue(parcelaConsultada.isPago());
        assertNotNull(parcelaConsultada.getDataPagamento());
    }

    @Test
    void deveRetornarErroAoTentarBaixarParcelaJaPaga() {
        GerarIptuRequest request = new GerarIptuRequest();
        request.setCidadaoId(CIDADAO_ID_TESTE);
        request.setAno(ANO_TESTE);
        request.setTipoPagamento(TipoPagamento.PARCELADO);
        iptuApiTestClient.gerarIptu(request);

        iptuApiTestClient.baixarParcela(CIDADAO_ID_TESTE, ANO_TESTE, 1);

        try {
            iptuApiTestClient.baixarParcela(CIDADAO_ID_TESTE, ANO_TESTE, 1);
            fail("Deveria ter lançado FeignException");
        } catch (feign.FeignException e) {
            assertEquals(HttpStatus.CONFLICT.value(), e.status());
        } catch (Exception e) {
            fail("Lançou uma exceção inesperada: " + e.getClass().getName());
        }
    }

    @Test
    void deveRetornarErroAoGerarIptuParaCidadaoInexistente() {

        GerarIptuRequest request = new GerarIptuRequest();
        request.setCidadaoId(CIDADAO_ID_TESTE + 1);
        request.setAno(ANO_TESTE);
        request.setTipoPagamento(TipoPagamento.PARCELADO);

        try {
            iptuApiTestClient.gerarIptu(request);
            fail("Deveria ter lançado FeignException");
        } catch (feign.FeignException e) {
            assertEquals(HttpStatus.BAD_REQUEST.value(), e.status());
            assertTrue(e.contentUTF8().contains("Cidadão com ID " + (CIDADAO_ID_TESTE + 1) + " não encontrado"));
        } catch (Exception e) {
            fail("Lançou uma exceção inesperada: " + e.getClass().getName());
        }
    }

}