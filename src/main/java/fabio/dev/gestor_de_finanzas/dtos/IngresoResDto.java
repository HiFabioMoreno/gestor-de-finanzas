package fabio.dev.gestor_de_finanzas.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IngresoResDto(
        Integer id,
        String tituloIngreso,
        String descripcionIngreso,
        BigDecimal cantidadIngreso,
        LocalDate fechaIngreso
) {
}
