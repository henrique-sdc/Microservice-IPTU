package com.springfield.iptu_service.controller;

import com.springfield.iptu_service.dto.GerarIptuRequest;
import com.springfield.iptu_service.dto.SituacaoIptuResponse;
import com.springfield.iptu_service.model.IptuAnual;
import com.springfield.iptu_service.model.ParcelaIPTU;
import com.springfield.iptu_service.service.IptuService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/iptu")
@CrossOrigin(origins = "*")
public class IptuController {

    private final IptuService iptuService;

    public IptuController(IptuService iptuService) {
        this.iptuService = iptuService;
    }

    @PostMapping("/gerar")
    public ResponseEntity<?> gerarIptu(@Valid @RequestBody GerarIptuRequest request) {
        try {
            IptuAnual iptuGerado = iptuService.gerarIptu(
                    request.getCidadaoId(),
                    request.getAno(),
                    request.getTipoPagamento());
            return ResponseEntity.status(HttpStatus.CREATED).body(iptuGerado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/{cidadaoId}/{ano}/situacao")
    public ResponseEntity<?> consultarSituacao(@PathVariable Integer cidadaoId, @PathVariable Integer ano) {
        try {
            SituacaoIptuResponse situacao = iptuService.consultarSituacaoIptu(cidadaoId, ano);
            return ResponseEntity.ok(situacao);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PutMapping("/{cidadaoId}/{ano}/parcela/{numeroParcela}/pagar")
    public ResponseEntity<?> baixarParcela(@PathVariable Integer cidadaoId,
            @PathVariable Integer ano,
            @PathVariable Integer numeroParcela) {
        try {
            ParcelaIPTU parcelaPaga = iptuService.baixarParcela(cidadaoId, ano, numeroParcela);
            return ResponseEntity.ok(Map.of(
                    "mensagem", "Parcela " + numeroParcela + " paga com sucesso!",
                    "parcela", parcelaPaga));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
            }
            if (e.getMessage().contains("já consta como paga")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}