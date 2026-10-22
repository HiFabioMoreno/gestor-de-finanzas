package fabio.dev.gestor_de_finanzas.dtos;

import fabio.dev.gestor_de_finanzas.modelos.CategoriasPredefinidas;

import java.time.LocalDate;
import java.util.List;

public record GastoResDto(
        Integer id,
        String tituloGasto,
        String descripcionGasto,
        List<CategoriasPredefinidas> categorias,
        Double cantidad,
        LocalDate fechaGasto
) {
}