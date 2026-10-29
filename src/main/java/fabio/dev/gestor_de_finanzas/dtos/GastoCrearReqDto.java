package fabio.dev.gestor_de_finanzas.dtos;

import fabio.dev.gestor_de_finanzas.modelos.CategoriasPredefinidas;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.List;

public record GastoCrearReqDto(
        @NotEmpty(message = "El nombre del gasto no puede estar vacio")
        String tituloGasto,
        @Length(max = 250)
        String descripcionGasto,
        List<CategoriasPredefinidas> categorias,
        @NotNull(message = "La cantidad total de la cuenta no puede estar vacio")
        @Positive
        BigDecimal cantidad
) {
}