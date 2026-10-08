package fabio.dev.gestor_de_finanzas.controlador;

import fabio.dev.gestor_de_finanzas.dtos.CuentaActualizadaResDto;
import fabio.dev.gestor_de_finanzas.dtos.CuentaActualizarReqDto;
import fabio.dev.gestor_de_finanzas.dtos.CuentaCrearReqDto;
import fabio.dev.gestor_de_finanzas.dtos.CuentaResDto;
import fabio.dev.gestor_de_finanzas.servicios.CuentaServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/gestorFinanzas/cuenta")
public class CuentaControlador {

    private final CuentaServicio cuentaServicio;

    public CuentaControlador(CuentaServicio cuentaServicio){
        this.cuentaServicio = cuentaServicio;
    }

    @GetMapping("/")
    public ResponseEntity<List<CuentaResDto>> obtenerCuentas(){
        return ResponseEntity.ok().body(this.cuentaServicio.listarCuentas());
    }

    @PostMapping("/")
    public ResponseEntity<CuentaResDto> crearCuenta(@Valid @RequestBody CuentaCrearReqDto cuentaCrearReqDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.cuentaServicio.crearCuenta(cuentaCrearReqDto));
    }

    @PatchMapping("/{id}/")
    public ResponseEntity<CuentaActualizadaResDto> actualizarCuenta(@PathVariable @Positive Integer id, @Valid @RequestBody CuentaActualizarReqDto actualizarReqDto){
        return ResponseEntity.status(HttpStatus.OK).body(this.cuentaServicio.actualizarCuenta(id, actualizarReqDto));
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<Void> eliminarCuenta(@PathVariable @Positive Integer id){
        this.cuentaServicio.eliminarCuenta(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
