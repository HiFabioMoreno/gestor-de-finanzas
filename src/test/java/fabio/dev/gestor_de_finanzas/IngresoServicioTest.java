package fabio.dev.gestor_de_finanzas;

import fabio.dev.gestor_de_finanzas.dtos.IngresoActualizarReqDto;
import fabio.dev.gestor_de_finanzas.dtos.IngresoCrearReqDto;
import fabio.dev.gestor_de_finanzas.dtos.IngresoResDto;
import fabio.dev.gestor_de_finanzas.excepciones.RecursoNoEncontradoException;
import fabio.dev.gestor_de_finanzas.modelos.Ingreso;
import fabio.dev.gestor_de_finanzas.repositorios.IngresoRepositorio;
import fabio.dev.gestor_de_finanzas.servicios.IngresoServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IngresoServicioTest {

    @Mock
    IngresoRepositorio ingresoRepositorio;

    @InjectMocks
    IngresoServicio ingresoServicio;

    private IngresoCrearReqDto ingresoResDto;

    @BeforeEach
    void setup(){

        MockitoAnnotations.openMocks(this);

        this.ingresoResDto = new IngresoCrearReqDto(
                "Pago de proyecto",
                null,
                new BigDecimal(24000)
        );
    }

    @Test
    @DisplayName("Deberia encontrar todos los ingresos exitosamente")
    public void encontrarIngresos_encontrarExitosamente_(){

        Ingreso ingresoUno = new Ingreso();
        ingresoUno.setId(1);
        ingresoUno.setTituloIngreso("Pago de proyecto");
        ingresoUno.setCantidadIngreso(new BigDecimal(2400));
        ingresoUno.setFechaIngreso(LocalDate.now());

        Ingreso ingresoDos = new Ingreso();
        ingresoDos.setId(2);
        ingresoDos.setTituloIngreso("Venta de papel");
        ingresoDos.setCantidadIngreso(new BigDecimal(200));
        ingresoDos.setFechaIngreso(LocalDate.now());

        when(this.ingresoRepositorio.findAll()).thenReturn(List.of(ingresoUno, ingresoDos));

        List<IngresoResDto> lista = this.ingresoServicio.buscarTodosIngresos();

        assertThat(lista.size()).isEqualTo(2);
        assertThat(lista.get(0).cantidadIngreso()).isEqualByComparingTo(new BigDecimal(2400));
        assertThat(lista.get(1).cantidadIngreso()).isEqualByComparingTo(new BigDecimal(200));
        assertThat(lista.get(0).tituloIngreso()).isEqualTo("Pago de proyecto");

        verify(this.ingresoRepositorio, times(1)).findAll();

    }

    @Test
    @DisplayName("Deberia agregar un nuevo ingreso exitosamente")
    public void agregarIngreso_deberiaAgregarIngresoExitosamente(){

        LocalDate fechaEsperada = LocalDate.now();

        doAnswer(invocation -> {
            Ingreso ingresoGuardado = invocation.getArgument(0);
            ingresoGuardado.setId(1);
            ingresoGuardado.setFechaIngreso(LocalDate.now());
            return ingresoGuardado;
        }).when(ingresoRepositorio).save(any(Ingreso.class));

        IngresoResDto resultado = ingresoServicio.agregarIngreso(this.ingresoResDto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.tituloIngreso()).isEqualTo(ingresoResDto.tituloIngreso());
        assertThat(resultado.cantidadIngreso()).isEqualTo(ingresoResDto.cantidad());
        assertThat(resultado.fechaIngreso()).isEqualTo(fechaEsperada);

        verify(ingresoRepositorio).save(any(Ingreso.class));

    }

    @Test
    @DisplayName("Deberia actualizar un ingreso exitosamente")
    public void actulizarIngreso_deberiaActulizarIngresoExitosamente(){

        Ingreso ingresoUno = new Ingreso();
        ingresoUno.setId(1);
        ingresoUno.setTituloIngreso("Pago de proyecto");
        ingresoUno.setCantidadIngreso(new BigDecimal(2400));
        ingresoUno.setFechaIngreso(LocalDate.now());

        when(ingresoRepositorio.findById(1)).thenReturn(Optional.of(ingresoUno));

        IngresoResDto res = ingresoServicio.actualizarIngreso(1, new IngresoActualizarReqDto(
                null,
                null,
                new BigDecimal(1000)
        ));

        assertThat(res).isNotNull();
        assertThat(ingresoUno.getCantidadIngreso()).isEqualTo(res.cantidadIngreso());
        assertThat(ingresoUno.getTituloIngreso()).isEqualTo("Pago de proyecto");

        verify(ingresoRepositorio, times(1)).findById(1);

    }

    @Test
    @DisplayName("No deberia actualizar un ingreso por ingreso no existente")
    public void actualizarIngreso_NoDeberiaActualizarIngreso(){

        when(ingresoRepositorio.findById(100)).thenReturn(Optional.empty());

        assertThatThrownBy(()->ingresoServicio.actualizarIngreso(100,new IngresoActualizarReqDto(
                null,
                null,
                new BigDecimal(1000)))).isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Ingreso no existe");

        verify(ingresoRepositorio, times(1)).findById(100);
        verify(ingresoRepositorio, never()).save(any());

    }

    @Test
    @DisplayName("Deberia eliminar un ingreso exitosamente")
    public void eliminarIngreso_deberiaEliminarIngresoExitosamente(){

        when(ingresoRepositorio.existsById(anyInt())).thenReturn(true);

        this.ingresoServicio.eliminarIngreso(anyInt());

        verify(ingresoRepositorio, times(1)).existsById(anyInt());
        verify(ingresoRepositorio, times(1)).deleteById(anyInt());

    }

    @Test
    @DisplayName("Deberia lanzar erro por id invalido")
    public void eliminarIngreso_lanzarErrorPorIdInvalido(){

        when(ingresoRepositorio.existsById(-10)).thenReturn(false);

        assertThatThrownBy(()->ingresoServicio.eliminarIngreso(-10))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Ingreso no existe");

        verify(ingresoRepositorio, never()).deleteById(anyInt());

    }

}