package com.vinico.repository;

import com.vinico.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByVinoId(Long vinoId);

    boolean existsByUsuarioIdAndVinoId(Long usuarioId, Long vinoId);

    @Query("SELECT AVG(r.puntuacion) FROM Resena r WHERE r.vino.id = :vinoId")
    Double promedioByVinoId(Long vinoId);
}
