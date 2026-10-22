package fabio.dev.gestor_de_finanzas;

import fabio.dev.gestor_de_finanzas.dtos.*;
import fabio.dev.gestor_de_finanzas.excepciones.RecursoNoEncontradoException;
import fabio.dev.gestor_de_finanzas.modelos.CategoriasPredefinidas;
import fabio.dev.gestor_de_finanzas.modelos.Gasto;
import fabio.dev.gestor_de_finanzas.repositorios.GastoRepositorio;
import fabio.dev.gestor_de_finanzas.servicios.GastoServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GastoServicioTest {

    @Mock
    GastoRepositorio gastoRepositorio;

    @InjectMocks
    GastoServicio gastoServicio;

    private GastoCrearReqDto requestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        requestDto = new GastoCrearReqDto(
                "Compra supermercado",
                "Compra semanal de víveres",
                List.of(CategoriasPredefinidas.COMIDA),
                150.75
        );

    }

    @Test
    @DisplayName("Debe guardar el gasto y retornar el DTO de respuesta con los datos correctos")
    void guardarGasto_debeGuardarYRetornarGastoResDto() {

        LocalDate fechaEsperada = LocalDate.now();

        doAnswer(invocation -> {
            Gasto gastoGuardado = invocation.getArgument(0);
            gastoGuardado.setId(1);
            gastoGuardado.setFechaGasto(fechaEsperada);
            return gastoGuardado;
        }).when(gastoRepositorio).save(any(Gasto.class));

        GastoResDto resultado = gastoServicio.guardarGasto(requestDto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.tituloGasto()).isEqualTo(requestDto.tituloGasto());
        assertThat(resultado.descripcionGasto()).isEqualTo(requestDto.descripcionGasto());
        assertThat(resultado.categorias()).isEqualTo(requestDto.categorias());
        assertThat(resultado.cantidad()).isEqualTo(requestDto.cantidad());
        assertThat(resultado.fechaGasto()).isEqualTo(fechaEsperada);

        ArgumentCaptor<Gasto> gastoCaptor = ArgumentCaptor.forClass(Gasto.class);
        verify(gastoRepositorio, times(1)).save(gastoCaptor.capture());

        Gasto gastoCapturado = gastoCaptor.getValue();
        assertThat(gastoCapturado.getTituloGasto()).isEqualTo(requestDto.tituloGasto());
        assertThat(gastoCapturado.getDescripcionGasto()).isEqualTo(requestDto.descripcionGasto());
        assertThat(gastoCapturado.getCategorias()).isEqualTo(requestDto.categorias());
        assertThat(gastoCapturado.getCantidad()).isEqualTo(requestDto.cantidad());

        verifyNoMoreInteractions(gastoRepositorio);
    }

    @Test
    @DisplayName("Debe propagar la excepción si el repositorio falla al guardar")
    void guardarGasto_cuandoRepositorioFalla_debePropagarExcepcion() {

        when(gastoRepositorio.save(any(Gasto.class)))
                .thenThrow(new RuntimeException("Error de base de datos"));


        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> gastoServicio.guardarGasto(requestDto)
        );

        verify(gastoRepositorio, times(1)).save(any(Gasto.class));
    }

    @Test
    @DisplayName("Debe de regresar todas los gastos exitosamente")
    void buscarTodosGastos_debeRegresarTodosLosGastos_Exitosamente(){

        Gasto gasto1 = new Gasto();
        gasto1.setId(1);
        gasto1.setFechaGasto(LocalDate.now());
        gasto1.setTituloGasto("Pago de emergencia");

        Gasto gasto2 = new Gasto();
        gasto2.setId(2);
        gasto2.setFechaGasto(LocalDate.now());
        gasto2.setTituloGasto("Compra supermercado");

        when(gastoRepositorio.findAll()).thenReturn(List.of(gasto1, gasto2));

        List<GastoResDto> resutlados = gastoServicio.buscarTodosGastos();

        assertThat(resutlados.size()).isEqualTo(2);
        assertThat(resutlados.get(0).tituloGasto()).isEqualTo("Pago de emergencia");
        assertThat(resutlados.get(1).tituloGasto()).isEqualTo("Compra supermercado");
        assertThat(resutlados.get(0).id()).isEqualTo(1);
        assertThat(resutlados.get(1).id()).isEqualTo(2);

        verify(gastoRepositorio, times(1)).findAll();

    }

    @Test
    @DisplayName("Debe retornar una lista vacía cuando no hay gastos registradas")
    void buscarTodosGastos_sinDatos_retornaListaVacia() {
        when(gastoRepositorio.findAll()).thenReturn(List.of());

        List<GastoResDto> resultado = gastoServicio.buscarTodosGastos();

        assertThat(resultado).isEmpty();
        verify(gastoRepositorio, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe de actulizar correctamente un gasto")
    void actualizarGasto_correctamente_deberiaActualizarCorrectamente() {

        Gasto gasto1 = new Gasto();
        gasto1.setId(1);
        gasto1.setTituloGasto("Pago de emergencia");
        gasto1.setCategorias(List.of(CategoriasPredefinidas.EMERGENCIA,CategoriasPredefinidas.SALUD));
        gasto1.setCantidad(2200.00);

        GastoActualizarReqDto actulizarDto = new GastoActualizarReqDto(
                null,
                null,
                null,
                1200.00);

        when(gastoRepositorio.findById(anyInt())).thenReturn(Optional.of(gasto1));

        GastoResDto resultado = gastoServicio.actualizarGasto(1, actulizarDto);
        assertThat(resultado).isNotNull();
        assertThat(resultado.tituloGasto()).isEqualTo("Pago de emergencia");
        assertThat(resultado.categorias()).isEqualTo(List.of(CategoriasPredefinidas.EMERGENCIA,CategoriasPredefinidas.SALUD));
        assertThat(resultado.cantidad()).isEqualTo(1200.00);

        verify(gastoRepositorio, times(1)).findById(anyInt());

    }


    @Test
    @DisplayName("Deberia actulizar dos campos a la vez exitosamente")
    void actualizarGasto_ActualizadDoscampos_a_la_vez_exitosamente() {
        Gasto gasto1 = new Gasto();
        gasto1.setId(1);
        gasto1.setTituloGasto("Pago de emergencia");
        gasto1.setCategorias(List.of(CategoriasPredefinidas.EMERGENCIA,CategoriasPredefinidas.SALUD));
        gasto1.setCantidad(2200.00);

        GastoActualizarReqDto actulizarDto = new GastoActualizarReqDto(
                "Pago de hospital",
                null,
                null,
                1200.00);

        when(gastoRepositorio.findById(anyInt())).thenReturn(Optional.of(gasto1));

        GastoResDto resultado = gastoServicio.actualizarGasto(1, actulizarDto);
        assertThat(resultado).isNotNull();
        assertThat(resultado.tituloGasto()).isEqualTo("Pago de hospital");
        assertThat(resultado.categorias()).isEqualTo(List.of(CategoriasPredefinidas.EMERGENCIA,CategoriasPredefinidas.SALUD));
        assertThat(resultado.cantidad()).isEqualTo(1200.00);

        verify(gastoRepositorio, times(1)).findById(anyInt());

    }

    @Test
    @DisplayName("No debe modificar nada cuando los campos vienen null")
    void actualizarCuenta_conAmbosNull_noModificaNada() {

        Gasto gasto1 = new Gasto();
        gasto1.setId(1);
        gasto1.setTituloGasto("Pago de emergencia");
        gasto1.setCategorias(List.of(CategoriasPredefinidas.EMERGENCIA,CategoriasPredefinidas.SALUD));
        gasto1.setCantidad(2200.00);

        GastoActualizarReqDto req = new GastoActualizarReqDto(null, null, null, null);

        when(gastoRepositorio.findById(1)).thenReturn(Optional.of(gasto1));

        GastoResDto resultado = gastoServicio.actualizarGasto(1, req);

        assertThat(resultado.tituloGasto()).isEqualTo("Pago de emergencia");
        assertThat(resultado.cantidad()).isEqualTo(2200.00);

        verify(gastoRepositorio, times(1)).findById(1);

    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando el gasto no existe")
    void actualizarCuenta_idInexistente_lanzaExcepcion() {

        GastoActualizarReqDto req = new GastoActualizarReqDto(null,null,null, 100.00);

        when(gastoRepositorio.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gastoServicio.actualizarGasto(99, req))
        .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");

        verify(gastoRepositorio, times(1)).findById(99);
        verify(gastoRepositorio, never()).save(any());

    }


    @Test
    @DisplayName("Debe de eliminar un gasto exitosamente")
    void eliminarGasto_debeelimnarungasto_exitosamente() {

        when(gastoRepositorio.existsById(anyInt())).thenReturn(true);

        gastoServicio.eliminarGasto(1);
        verify(gastoRepositorio, times(1)).existsById(anyInt());
        verify(gastoRepositorio, times(1)).deleteById(anyInt());

    }


    @Test
    @DisplayName("Debe lanzar excepcion cuando el gasto no existe ")
    void  eliminarGasto_debeelimnarungasto_existente() {

        when(gastoRepositorio.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> gastoServicio.eliminarGasto(99))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");

        verify(gastoRepositorio, times(1)).existsById(99);
        verify(gastoRepositorio, never()).deleteById(anyInt());
    }



}