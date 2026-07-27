package fabio.dev.gestor_de_finanzas.servicios;

import fabio.dev.gestor_de_finanzas.dtos.CuentaActualizadaResDto;
import fabio.dev.gestor_de_finanzas.dtos.CuentaActualizarReqDto;
import fabio.dev.gestor_de_finanzas.dtos.CuentaCrearReqDto;
import fabio.dev.gestor_de_finanzas.dtos.CuentaResDto;
import fabio.dev.gestor_de_finanzas.excepciones.RecursoNoEncontradoException;
import fabio.dev.gestor_de_finanzas.modelos.Cuenta;
import fabio.dev.gestor_de_finanzas.repositorios.CuentaRepositorio;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class CuentaServicio {

    private CuentaRepositorio cuentaRepositorio;

    private Logger logger = LoggerFactory.getLogger(CuentaServicio.class);

    public CuentaServicio(CuentaRepositorio cuentaRepositorio) {
        this.cuentaRepositorio = cuentaRepositorio;
    }

    @Transactional
    public CuentaResDto crearCuenta(CuentaCrearReqDto cuentaCrearReqDto) {

        logger.info("Creando una nueva cuenta");

        Cuenta cuenta = new Cuenta();
        cuenta.setTituloCuenta( cuentaCrearReqDto.tituloCuenta());
        cuenta.setDescripcionCuenta(cuentaCrearReqDto.descripcionCuenta());
        cuenta.setTotal(0.00);

        cuentaRepositorio.save(cuenta);

        logger.info("Cuenta creada correctamente");

        return new CuentaResDto(
                cuenta.getTituloCuenta(),
                cuenta.getDescripcionCuenta(),
                cuenta.getTotal(),
                cuenta.getFechaCuentaCreada());
    }

    public List<CuentaResDto> listarCuentas(){
        logger.info("Listando todos los cuentas");

        List<Cuenta> cuentas = cuentaRepositorio.findAll();

        List<CuentaResDto> listaCuentas = new ArrayList<>();

        cuentas.forEach(cuenta -> listaCuentas.add(
                new CuentaResDto(
                cuenta.getTituloCuenta(),
                cuenta.getDescripcionCuenta(),
                cuenta.getTotal(),
                cuenta.getFechaCuentaCreada()
        )));

        return listaCuentas;
    }

    @Transactional
    public CuentaActualizadaResDto actualizarCuenta(Integer idCuenta, CuentaActualizarReqDto cuentaActualizarReqDto) {

        logger.info("Actualizando una nueva cuenta");

        Cuenta cuenta = cuentaRepositorio.findById(idCuenta).orElseThrow(()-> new RecursoNoEncontradoException("No existe la cuenta con id: " + idCuenta));

        if (cuentaActualizarReqDto.tituloCuenta() != null){
            cuenta.setTituloCuenta(cuentaActualizarReqDto.tituloCuenta());
        }

        if (cuentaActualizarReqDto.descripcionCuenta() != null){
            cuenta.setDescripcionCuenta(cuentaActualizarReqDto.descripcionCuenta());
        }

        logger.info("Actualizacion correcta de la cuenta con id: {}",cuenta.getId());

        return new CuentaActualizadaResDto(
                cuenta.getTituloCuenta(),
                cuenta.getDescripcionCuenta()
        );
    }


    @Transactional
    public void eliminarCuenta(Integer idCuenta) {
        logger.info("Eliminando una nueva cuenta");

        if(!cuentaRepositorio.existsById(idCuenta)){
            throw new RecursoNoEncontradoException("No existe el cuenta con el id: " + idCuenta);
        }

        cuentaRepositorio.deleteById(idCuenta);

    }

}
