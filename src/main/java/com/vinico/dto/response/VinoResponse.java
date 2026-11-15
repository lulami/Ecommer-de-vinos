package com.vinico.dto.response;

import com.vinico.model.Vino;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class VinoResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private String tipo;
    private String bodega;
    private String region;
    private Integer anio;
    private BigDecimal precio;
    private Integer stock;
    private String imagenUrl;
    private Boolean activo;

    public static VinoResponse fromEntity(Vino vino) {
        return VinoResponse.builder()
                .id(vino.getId())
                .nombre(vino.getNombre())
                .descripcion(vino.getDescripcion())
                .tipo(vino.getTipo().name())
                .bodega(vino.getBodega())
                .region(vino.getRegion())
                .anio(vino.getAnio())
                .precio(vino.getPrecio())
                .stock(vino.getStock())
                .imagenUrl(vino.getImagenUrl())
                .activo(vino.getActivo())
                .build();
    }
}
