package fabio.dev.gestor_de_finanzas.dtos;

import fabio.dev.gestor_de_finanzas.modelos.CategoriasPredefinidas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record GastoResDto(
        Integer id,
        String tituloGasto,
        String descripcionGasto,
        List<CategoriasPredefinidas> categorias,
        BigDecimal cantidad,
        LocalDate fechaGasto
) {
}