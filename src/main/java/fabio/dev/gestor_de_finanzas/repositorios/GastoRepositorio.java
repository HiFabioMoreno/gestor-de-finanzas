package fabio.dev.gestor_de_finanzas.repositorios;

import fabio.dev.gestor_de_finanzas.modelos.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GastoRepositorio extends JpaRepository<Gasto, Integer> {
}
