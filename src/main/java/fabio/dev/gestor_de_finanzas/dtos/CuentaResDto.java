package fabio.dev.gestor_de_finanzas.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CuentaResDto(
        Integer id,
        String tituloCuenta,
        String descripcionCuenta,
        BigDecimal total,
        LocalDate fechaCuentaCreada
) {
}
