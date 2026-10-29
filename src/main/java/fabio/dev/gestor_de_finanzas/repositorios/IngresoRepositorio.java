package fabio.dev.gestor_de_finanzas.repositorios;

import fabio.dev.gestor_de_finanzas.modelos.Ingreso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngresoRepositorio extends JpaRepository<Ingreso, Integer> {
}
