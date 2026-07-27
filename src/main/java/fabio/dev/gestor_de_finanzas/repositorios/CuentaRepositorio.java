package fabio.dev.gestor_de_finanzas.repositorios;

import fabio.dev.gestor_de_finanzas.modelos.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaRepositorio extends JpaRepository<Cuenta, Integer> {
}
