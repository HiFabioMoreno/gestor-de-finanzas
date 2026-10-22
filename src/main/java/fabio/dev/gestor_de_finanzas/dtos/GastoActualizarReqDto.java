package fabio.dev.gestor_de_finanzas.dtos;

import fabio.dev.gestor_de_finanzas.modelos.CategoriasPredefinidas;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record GastoActualizarReqDto(

        @Length(max = 250)
        String tituloGasto,

        @Length(max = 250)
        String descripcionGasto,

        List<CategoriasPredefinidas> categorias,
        @Positive
        Double cantidad
) {
}