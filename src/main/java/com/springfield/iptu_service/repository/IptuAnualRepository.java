package com.springfield.iptu_service.repository;

import com.springfield.iptu_service.model.IptuAnual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IptuAnualRepository extends JpaRepository<IptuAnual, Integer> {

    boolean existsByCidadaoIdAndAno(Integer cidadaoId, Integer ano);

    Optional<IptuAnual> findByCidadaoIdAndAno(Integer cidadaoId, Integer ano);

    List<IptuAnual> findByCidadaoId(Integer cidadaoId);
}