package fabio.dev.gestor_de_finanzas.controlador;

import fabio.dev.gestor_de_finanzas.dtos.IngresoActualizarReqDto;
import fabio.dev.gestor_de_finanzas.dtos.IngresoCrearReqDto;
import fabio.dev.gestor_de_finanzas.dtos.IngresoResDto;
import fabio.dev.gestor_de_finanzas.servicios.IngresoServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/gestorFinanzas/ingresos")
public class IngresoControlador {

    private final IngresoServicio ingresoServicio;

    public IngresoControlador(IngresoServicio ingresoServicio){
        this.ingresoServicio = ingresoServicio;
    }

    @GetMapping("/")
    public ResponseEntity<List<IngresoResDto>> mostrarIngresos(){
        return ResponseEntity.status(HttpStatus.OK).body(this.ingresoServicio.buscarTodosIngresos());
    }

    @PostMapping("/")
    public ResponseEntity<IngresoResDto> guardarIngreso(@Valid @RequestBody IngresoCrearReqDto dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.ingresoServicio.agregarIngreso(dto));
    }

    @PatchMapping("/ingreso/{id}/")
    public ResponseEntity<IngresoResDto> actualizarIngreso(@PathVariable @Positive Integer id,
                                                           @Valid @RequestBody IngresoActualizarReqDto dto){
        return ResponseEntity.status(HttpStatus.OK).body(ingresoServicio.actualizarIngreso(id, dto));
    }

    @DeleteMapping("/ingreso/{id}/")
    public ResponseEntity<Void> eliminarIngreso(@PathVariable @Positive Integer id){
        this.ingresoServicio.eliminarIngreso(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}