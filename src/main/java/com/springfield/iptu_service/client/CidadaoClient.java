package com.springfield.iptu_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cidadao-service", url = "${cidadao.service.url}")
public interface CidadaoClient {

    @GetMapping("/cidadaos/{id}")
    ResponseEntity<Void> verificarCidadaoExiste(@PathVariable("id") Integer id);
}