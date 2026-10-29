package fabio.dev.gestor_de_finanzas.servicios;

import fabio.dev.gestor_de_finanzas.dtos.IngresoActualizarReqDto;
import fabio.dev.gestor_de_finanzas.dtos.IngresoCrearReqDto;
import fabio.dev.gestor_de_finanzas.dtos.IngresoResDto;
import fabio.dev.gestor_de_finanzas.excepciones.RecursoNoEncontradoException;
import fabio.dev.gestor_de_finanzas.modelos.Ingreso;
import fabio.dev.gestor_de_finanzas.repositorios.IngresoRepositorio;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IngresoServicio {

    private IngresoRepositorio ingresoRepositorio;

    private final Logger logger = LoggerFactory.getLogger(IngresoServicio.class);

    public IngresoServicio (IngresoRepositorio ingresoRepositorio){
        this.ingresoRepositorio = ingresoRepositorio;
    }

    public List<IngresoResDto> buscarTodosIngresos(){

        logger.info("Buscando ingresos");

        List<Ingreso> ingresos = ingresoRepositorio.findAll();

        List<IngresoResDto> ingresoRes =  new ArrayList<>();

        ingresos.forEach(ingreso -> {ingresoRes.add(
                new IngresoResDto(
                        ingreso.getId(),
                        ingreso.getTituloIngreso(),
                        ingreso.getDescripcionIngreso(),
                        ingreso.getCantidadIngreso(),
                        ingreso.getFechaIngreso()
                ));
        });

        logger.info("Ingresos encontrados exitosamente");

        return ingresoRes;
    }

    @Transactional
    public IngresoResDto agregarIngreso(IngresoCrearReqDto dto){

        logger.info("Agregando Ingreso...");

        Ingreso ingreso = new Ingreso();
        ingreso.setTituloIngreso(dto.tituloIngreso());
        ingreso.setDescripcionIngreso(dto.descripcion());
        ingreso.setCantidadIngreso(dto.cantidad());

        this.ingresoRepositorio.save(ingreso);

        logger.info("Ingreso agregado exitosamente");

        return new IngresoResDto(
                ingreso.getId(),
                ingreso.getTituloIngreso(),
                ingreso.getDescripcionIngreso(),
                ingreso.getCantidadIngreso(),
                ingreso.getFechaIngreso()
        );

    }

    @Transactional
    public IngresoResDto actualizarIngreso(Integer id, IngresoActualizarReqDto dto){

        Ingreso ingreso = ingresoRepositorio.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Ingreso no existe"));

        if (dto.cantidad() != null){ingreso.setCantidadIngreso(dto.cantidad());}
        if (dto.descripcion() != null){ingreso.setDescripcionIngreso(dto.descripcion());}
        if (dto.titulo() != null){ingreso.setTituloIngreso(dto.titulo());}

        logger.info("Actualizacion de ingreso exitosamente");

        ingresoRepositorio.save(ingreso);

        return new IngresoResDto(
                ingreso.getId(),
                ingreso.getTituloIngreso(),
                ingreso.getDescripcionIngreso(),
                ingreso.getCantidadIngreso(),
                ingreso.getFechaIngreso()
        );
    }

    @Transactional
    public void eliminarIngreso(Integer id){

        logger.info("Eliminado ingreso...");

        if (!this.ingresoRepositorio.existsById(id)){
            throw new RecursoNoEncontradoException("Ingreso no existe");
        }

        this.ingresoRepositorio.deleteById(id);

        logger.info("Ingreso con id: {} eliminado exitosamente", id);
    }

}