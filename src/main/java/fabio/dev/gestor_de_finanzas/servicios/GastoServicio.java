package fabio.dev.gestor_de_finanzas.servicios;

import fabio.dev.gestor_de_finanzas.dtos.GastoActualizarReqDto;
import fabio.dev.gestor_de_finanzas.dtos.GastoCrearReqDto;
import fabio.dev.gestor_de_finanzas.dtos.GastoResDto;
import fabio.dev.gestor_de_finanzas.excepciones.RecursoNoEncontradoException;
import fabio.dev.gestor_de_finanzas.modelos.Gasto;
import fabio.dev.gestor_de_finanzas.repositorios.GastoRepositorio;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GastoServicio {

    private final GastoRepositorio gastoRepositorio;

    private Logger log = LoggerFactory.getLogger(GastoServicio.class);

    public GastoServicio(GastoRepositorio gastoRepositorio) {
        this.gastoRepositorio = gastoRepositorio;
    }

    @Transactional
    public GastoResDto guardarGasto(GastoCrearReqDto gastoCrearReqDto) {

        log.info("Agregando gasto: {}", gastoCrearReqDto.tituloGasto());

        Gasto gasto = new Gasto();
        gasto.setTituloGasto(gastoCrearReqDto.tituloGasto());
        gasto.setDescripcionGasto(gastoCrearReqDto.descripcionGasto());
        gasto.setCategorias(gastoCrearReqDto.categorias());
        gasto.setCantidad(gastoCrearReqDto.cantidad());

        this.gastoRepositorio.save(gasto);

        log.info("Gasto agregado exitosamente con ID: {}", gasto.getId());

        return new GastoResDto(
                gasto.getId(),
                gasto.getTituloGasto(),
                gasto.getDescripcionGasto(),
                gasto.getCategorias(),
                gasto.getCantidad(),
                gasto.getFechaGasto()
        );
    }

    public List<GastoResDto> buscarTodosGastos() {
        log.info("Buscando todos los gastos");

        List<Gasto> gastos = this.gastoRepositorio.findAll();

        List<GastoResDto> gastosDto = new ArrayList<>();

        gastos.forEach(
                gsto -> { gastosDto.add(
                    new GastoResDto(
                            gsto.getId(),
                            gsto.getTituloGasto(),
                            gsto.getDescripcionGasto(),
                            gsto.getCategorias(),
                            gsto.getCantidad(),
                            gsto.getFechaGasto()
                    ));
                });

        log.info("Mostrando los {} gastos exitosamente", gastosDto.size());

        return gastosDto;
    }

    @Transactional
    public GastoResDto actualizarGasto(Integer id, GastoActualizarReqDto gastoReqDto) {

        Gasto gasto = gastoRepositorio.findById(id).orElseThrow(()-> new RecursoNoEncontradoException("No existe el gasto con el id: " + id));

        if (gastoReqDto.tituloGasto() != null) {gasto.setTituloGasto(gastoReqDto.tituloGasto());}
        if (gastoReqDto.descripcionGasto() != null) {gasto.setDescripcionGasto(gastoReqDto.descripcionGasto());}
        if (gastoReqDto.categorias() != null) {gasto.setCategorias(gastoReqDto.categorias());}
        if (gastoReqDto.cantidad() != null) {gasto.setCantidad(gastoReqDto.cantidad());}

        log.info("Gasto actualizado exitosamente con ID: {}", id);

        return new GastoResDto(
                gasto.getId(),
                gasto.getTituloGasto(),
                gasto.getDescripcionGasto(),
                gasto.getCategorias(),
                gasto.getCantidad(),
                gasto.getFechaGasto());
    }

    @Transactional
    public void eliminarGasto(Integer id) {
        log.info("Eliminando gasto: {}", id);

        if (!gastoRepositorio.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el gasto con el id: " + id);
        }

        log.info("Eliminando gasto exitosamente con el id: {}", id);

        this.gastoRepositorio.deleteById(id);
    }

}