package com.vinico.controller;

import com.vinico.dto.request.VinoRequest;
import com.vinico.dto.response.VinoResponse;
import com.vinico.model.Vino;
import com.vinico.service.VinoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
public class VinoController {

    private final VinoService vinoService;

    // GET /api/productos
    // GET /api/productos?tipo=TINTO
    @GetMapping
    public ResponseEntity<List<VinoResponse>> listar(
            @RequestParam(required = false) String tipo) {

        if (tipo != null) {
            Vino.TipoVino tipoVino = Vino.TipoVino.valueOf(tipo.toUpperCase());
            return ResponseEntity.ok(vinoService.listarPorTipo(tipoVino));
        }
        return ResponseEntity.ok(vinoService.listarTodos());
    }

    // GET /api/productos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<VinoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vinoService.buscarPorId(id));
    }

    // POST /api/admin/productos
    @PostMapping("/admin")
    public ResponseEntity<VinoResponse> crear(@Valid @RequestBody VinoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vinoService.crear(request));
    }

    // PUT /api/admin/productos/{id}
    @PutMapping("/admin/{id}")
    public ResponseEntity<VinoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody VinoRequest request) {
        return ResponseEntity.ok(vinoService.actualizar(id, request));
    }

    // PUT /api/admin/productos/{id}/stock
    @PutMapping("/admin/{id}/stock")
    public ResponseEntity<Void> actualizarStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        vinoService.actualizarStock(id, body.get("stock"));
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/admin/productos/{id}
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        vinoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
