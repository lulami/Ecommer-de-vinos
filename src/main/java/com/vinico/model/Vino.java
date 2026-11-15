package com.vinico.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "vinos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @NotNull(message = "El tipo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVino tipo;

    private String bodega;

    private String region;

    @Min(value = 1900, message = "El año debe ser mayor a 1900")
    @Max(value = 2100, message = "Año inválido")
    private Integer anio;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private Integer stock = 0;

    private String imagenUrl;

    @Column(nullable = false)
    private Boolean activo = true;

    public enum TipoVino {
        TINTO, BLANCO, ROSADO, ESPUMANTE
    }
}
