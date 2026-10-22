package fabio.dev.gestor_de_finanzas.controlador;

import fabio.dev.gestor_de_finanzas.dtos.GastoActualizarReqDto;
import fabio.dev.gestor_de_finanzas.dtos.GastoCrearReqDto;
import fabio.dev.gestor_de_finanzas.dtos.GastoResDto;
import fabio.dev.gestor_de_finanzas.servicios.GastoServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/gestorFinanzas/gastos")
public class GastoControlador {

    private final GastoServicio gastoServicio;

    public GastoControlador (GastoServicio gastoServicio){
        this.gastoServicio = gastoServicio;
    }

    @GetMapping("/")
    public ResponseEntity<List<GastoResDto>> buscarGastos(){
        return ResponseEntity.status(HttpStatus.OK).body(this.gastoServicio.buscarTodosGastos());
    }

    @PostMapping("/")
    public ResponseEntity<GastoResDto> crearGasto(@Valid @RequestBody GastoCrearReqDto gastoCrearReqDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.gastoServicio.guardarGasto(gastoCrearReqDto));
    }

    @PatchMapping("/gasto/{id}/")
    public ResponseEntity<GastoResDto> actualizarGasto(@PathVariable @Positive Integer id, @Valid @RequestBody GastoActualizarReqDto gastoActualizarReqDto){
        return ResponseEntity.ok().body(this.gastoServicio.actualizarGasto(id, gastoActualizarReqDto));
    }

    @DeleteMapping("/gasto/{id}/")
    public ResponseEntity<Void> eliminarGasto(@PathVariable @Positive Integer id){
        this.gastoServicio.eliminarGasto(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
