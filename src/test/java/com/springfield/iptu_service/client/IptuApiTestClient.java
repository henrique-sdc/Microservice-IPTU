package com.springfield.iptu_service.client;

import com.springfield.iptu_service.dto.GerarIptuRequest;
import com.springfield.iptu_service.dto.SituacaoIptuResponse;
import com.springfield.iptu_service.model.IptuAnual;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "iptu-test-client", url = "http://localhost:${server.port}")
public interface IptuApiTestClient {

    @PostMapping("/iptu/gerar")
    ResponseEntity<IptuAnual> gerarIptu(@RequestBody GerarIptuRequest request);

    @GetMapping("/iptu/{cidadaoId}/{ano}/situacao")
    ResponseEntity<SituacaoIptuResponse> consultarSituacao(@PathVariable("cidadaoId") Integer cidadaoId,
            @PathVariable("ano") Integer ano);

    @PutMapping("/iptu/{cidadaoId}/{ano}/parcela/{numeroParcela}/pagar")
    ResponseEntity<Map<String, Object>> baixarParcela(@PathVariable("cidadaoId") Integer cidadaoId,
            @PathVariable("ano") Integer ano,
            @PathVariable("numeroParcela") Integer numeroParcela);
}