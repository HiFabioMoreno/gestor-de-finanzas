package fabio.dev.gestor_de_finanzas.dtos;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record IngresoActualizarReqDto(
        String titulo,
        String descripcion,
        @Positive
        BigDecimal cantidad
) {
}
