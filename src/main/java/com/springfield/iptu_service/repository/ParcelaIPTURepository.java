package com.springfield.iptu_service.repository;

import com.springfield.iptu_service.model.ParcelaIPTU;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelaIPTURepository extends JpaRepository<ParcelaIPTU, Integer> {

    List<ParcelaIPTU> findByIptuAnualIdOrderByNumeroParcelaAsc(Integer iptuAnualId);

    Optional<ParcelaIPTU> findByIptuAnualIdAndNumeroParcela(Integer iptuAnualId, Integer numeroParcela);

    @Query("SELECT SUM(p.valor) FROM ParcelaIPTU p WHERE p.iptuAnual.id = :iptuAnualId AND p.pago = true")
    Optional<BigDecimal> sumValorPagoByIptuAnualId(@Param("iptuAnualId") Integer iptuAnualId);

    @Query("SELECT SUM(p.valor) FROM ParcelaIPTU p WHERE p.iptuAnual.id = :iptuAnualId AND p.pago = false")
    Optional<BigDecimal> sumValorDevidoByIptuAnualId(@Param("iptuAnualId") Integer iptuAnualId);
}