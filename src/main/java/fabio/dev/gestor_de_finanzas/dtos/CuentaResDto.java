package fabio.dev.gestor_de_finanzas.dtos;

import java.time.LocalDate;

public record CuentaResDto(
        Integer id,
        String tituloCuenta,
        String descripcionCuenta,
        Double total,
        LocalDate fechaCuentaCreada
) {
}
