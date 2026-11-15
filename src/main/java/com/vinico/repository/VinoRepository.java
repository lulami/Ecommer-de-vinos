package com.vinico.repository;

import com.vinico.model.Vino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VinoRepository extends JpaRepository<Vino, Long> {

    List<Vino> findByActivoTrue();

    List<Vino> findByTipoAndActivoTrue(Vino.TipoVino tipo);

    List<Vino> findByStockGreaterThanAndActivoTrue(Integer stock);

    boolean existsByNombreAndBodega(String nombre, String bodega);
}
