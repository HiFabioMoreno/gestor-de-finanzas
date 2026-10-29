package fabio.dev.gestor_de_finanzas.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record IngresoCrearReqDto(
        @NotBlank
        String tituloIngreso,
        String descripcion,
        @Positive
        BigDecimal cantidad
) {
}
