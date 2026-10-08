package fabio.dev.gestor_de_finanzas.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CuentaCrearReqDto(
        @NotBlank(message = "El titulo de la cuenta no puede estar vacío")
        String tituloCuenta,
        String descripcionCuenta,
        @NotNull(message = "El total no puede ser nulo")
        @PositiveOrZero(message = "El total no puede ser negativo")
        Double total
) {
}
