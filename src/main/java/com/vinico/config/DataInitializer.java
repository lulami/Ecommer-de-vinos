package com.vinico.config;

import com.vinico.model.*;
import com.vinico.model.KardexBottle.WineBottleStatus;
import com.vinico.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final VinoRepository vinoRepository;
    private final VaultNicheRepository vaultNicheRepository;
    private final KardexBottleRepository bottleRepository;

    @Override
    public void run(String... args) {
        seedVinos();
        seedKardex();
    }

    // ─────────────────── Vinos ───────────────────

    private void seedVinos() {
        if (vinoRepository.count() > 0) return;

        List<Vino> vinos = List.of(
            Vino.builder().nombre("Malbec Reserva")
                .descripcion("Tinto intenso con notas a frutas rojas y vainilla")
                .tipo(Vino.TipoVino.TINTO).bodega("Zuccardi").region("Mendoza")
                .anio(2021).precio(new BigDecimal("4500")).stock(50)
                .imagenUrl("vino1.svg").activo(true).build(),
            Vino.builder().nombre("Cabernet Sauvignon")
                .descripcion("Cuerpo pleno, taninos firmes y larga persistencia")
                .tipo(Vino.TipoVino.TINTO).bodega("Catena Zapata").region("Luján de Cuyo")
                .anio(2020).precio(new BigDecimal("6200")).stock(30)
                .imagenUrl("vino2.svg").activo(true).build(),
            Vino.builder().nombre("Torrontés Clásico")
                .descripcion("Floral y fresco, perfecto para mariscos")
                .tipo(Vino.TipoVino.BLANCO).bodega("Clos de los Siete").region("Salta")
                .anio(2023).precio(new BigDecimal("3800")).stock(40)
                .imagenUrl("vino3.svg").activo(true).build(),
            Vino.builder().nombre("Chardonnay Barrel")
                .descripcion("Crianza en roble, notas de manteca y fruta tropical")
                .tipo(Vino.TipoVino.BLANCO).bodega("Norton").region("Valle de Uco")
                .anio(2022).precio(new BigDecimal("5100")).stock(25)
                .imagenUrl("vino4.svg").activo(true).build(),
            Vino.builder().nombre("Rosado de Malbec")
                .descripcion("Seco y elegante, color rosa brillante")
                .tipo(Vino.TipoVino.ROSADO).bodega("Achaval Ferrer").region("Mendoza")
                .anio(2023).precio(new BigDecimal("3200")).stock(35)
                .imagenUrl("vino5.svg").activo(true).build(),
            Vino.builder().nombre("Extra Brut Nature")
                .descripcion("Espumante método champenoise, burbuja fina y persistente")
                .tipo(Vino.TipoVino.ESPUMANTE).bodega("Chandon").region("Agrelo")
                .anio(2022).precio(new BigDecimal("7500")).stock(20)
                .imagenUrl("vino6.svg").activo(true).build()
        );

        vinoRepository.saveAll(vinos);
    }

    // ─────────────────── Kardex — Vault Niches + Bottles ───────────────────

    private void seedKardex() {
        if (vaultNicheRepository.count() > 0) return;

        // Crear 4 nichos (zonas de la bodega)
        VaultNiche nicheA = createNiche("NICHE-A1", "ZONA_TINTO", new BigDecimal("14.0"), new BigDecimal("16.0"));
        VaultNiche nicheB = createNiche("NICHE-B1", "ZONA_BLANCO", new BigDecimal("10.0"), new BigDecimal("12.0"));
        VaultNiche nicheC = createNiche("NICHE-C1", "ZONA_ESPUMANTE", new BigDecimal("8.0"), new BigDecimal("10.0"));
        VaultNiche nicheD = createNiche("NICHE-D1", "ZONA_PREMIUM", new BigDecimal("15.0"), new BigDecimal("17.0"));

        vaultNicheRepository.saveAll(List.of(nicheA, nicheB, nicheC, nicheD));

        // Botellas en NICHE-A1 (Malbec/Cabernet)
        seedBottle("SKU-MALBEC-2021", 2021, nicheA, 1, "SEAL-MA-001");
        seedBottle("SKU-MALBEC-2021", 2021, nicheA, 2, "SEAL-MA-002");
        seedBottle("SKU-MALBEC-2021", 2021, nicheA, 3, "SEAL-MA-003");
        seedBottle("SKU-MALBEC-2021", 2021, nicheA, 4, "SEAL-MA-004");
        seedBottle("SKU-CAB-2020",    2020, nicheA, 5, "SEAL-CB-001");
        seedBottle("SKU-CAB-2020",    2020, nicheA, 6, "SEAL-CB-002");

        // Botellas en NICHE-B1 (Torrontés/Chardonnay)
        seedBottle("SKU-TORR-2023",  2023, nicheB, 1, "SEAL-TR-001");
        seedBottle("SKU-TORR-2023",  2023, nicheB, 2, "SEAL-TR-002");
        seedBottle("SKU-CHARD-2022", 2022, nicheB, 3, "SEAL-CH-001");
        seedBottle("SKU-CHARD-2022", 2022, nicheB, 4, "SEAL-CH-002");

        // Botellas en NICHE-C1 (Espumante)
        seedBottle("SKU-BRUT-2022", 2022, nicheC, 1, "SEAL-BR-001");
        seedBottle("SKU-BRUT-2022", 2022, nicheC, 2, "SEAL-BR-002");
        seedBottle("SKU-BRUT-2022", 2022, nicheC, 3, "SEAL-BR-003");

        // Botellas en NICHE-D1 (Premium — Malbec y Cabernet de alta gama)
        seedBottle("SKU-MALBEC-2021", 2021, nicheD, 1, "SEAL-PM-001");
        seedBottle("SKU-CAB-2020",    2020, nicheD, 2, "SEAL-PC-001");
    }

    private VaultNiche createNiche(String id, String zone, BigDecimal tempMin, BigDecimal tempMax) {
        VaultNiche n = new VaultNiche();
        n.setNicheId(id);
        n.setZoneSector(zone);
        n.setTotalSlots(18);
        n.setTargetTempMin(tempMin);
        n.setTargetTempMax(tempMax);
        return n;
    }

    private void seedBottle(String sku, int vintage, VaultNiche niche, int slot, String seal) {
        KardexBottle b = new KardexBottle();
        b.setBottleId(UUID.randomUUID().toString());
        b.setSku(sku);
        b.setBottleSerial(seal + "-" + System.nanoTime());
        b.setVintageYear(vintage);
        b.setVaultNiche(niche);
        b.setSlotIndex(slot);
        b.setStatus(WineBottleStatus.IN_VAULT_AVAILABLE);
        b.setSealNumber(seal);
        b.setCurrentOrderId(null);
        bottleRepository.save(b);
    }
}
