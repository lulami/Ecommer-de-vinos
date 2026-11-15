package com.vinico.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VinoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;  // TINTO, BLANCO, ROSADO, ESPUMANTE

    private String bodega;
    private String region;

    @Min(value = 1900, message = "El año debe ser mayor a 1900")
    private Integer anio;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock = 0;

    private String imagenUrl;
}
