package com.vinico.repository;

import com.vinico.model.KardexBottle;
import com.vinico.model.KardexBottle.WineBottleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KardexBottleRepository extends JpaRepository<KardexBottle, String> {

    /**
     * Busca botellas disponibles por SKU y año de cosecha.
     * Se usa PESSIMISTIC_WRITE para simular SELECT ... FOR UPDATE en H2.
     */
    @Query("SELECT b FROM KardexBottle b WHERE b.sku = :sku AND b.vintageYear = :vintage AND b.status = :status")
    List<KardexBottle> findAvailableBySkuAndVintage(
            @Param("sku") String sku,
            @Param("vintage") Integer vintage,
            @Param("status") WineBottleStatus status);

    List<KardexBottle> findByVaultNicheNicheIdAndStatus(String nicheId, WineBottleStatus status);

    long countByVaultNicheNicheIdAndStatus(String nicheId, WineBottleStatus status);

    List<KardexBottle> findByStatus(WineBottleStatus status);
}
