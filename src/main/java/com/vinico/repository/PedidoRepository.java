package com.vinico.repository;

import com.vinico.model.Pedido;
import com.vinico.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioOrderByFechaPedidoDesc(Usuario usuario);

    List<Pedido> findByEstado(Pedido.EstadoPedido estado);

    @Query("SELECT p FROM Pedido p JOIN FETCH p.items i JOIN FETCH i.vino WHERE p.id = :id")
    java.util.Optional<Pedido> findByIdWithItems(Long id);
}
