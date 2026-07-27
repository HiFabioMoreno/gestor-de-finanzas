package fabio.dev.gestor_de_finanzas;

import fabio.dev.gestor_de_finanzas.dtos.CuentaActualizadaResDto;
import fabio.dev.gestor_de_finanzas.dtos.CuentaActualizarReqDto;
import fabio.dev.gestor_de_finanzas.dtos.CuentaCrearReqDto;
import fabio.dev.gestor_de_finanzas.dtos.CuentaResDto;
import fabio.dev.gestor_de_finanzas.excepciones.RecursoNoEncontradoException;
import fabio.dev.gestor_de_finanzas.modelos.Cuenta;
import fabio.dev.gestor_de_finanzas.repositorios.CuentaRepositorio;
import fabio.dev.gestor_de_finanzas.servicios.CuentaServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CuentaServicioTest {

    @Mock
    private CuentaRepositorio cuentaRepositorio;

    @InjectMocks
    private CuentaServicio cuentaServicio;

    private Cuenta cuenta;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        cuenta = new Cuenta();
        cuenta.setId(1);
        cuenta.setTituloCuenta("Gastos de la casa");
        cuenta.setDescripcionCuenta("Cuenta para administrar los gastos de la casa");
        cuenta.setTotal(0.00);

    }

    @Test
    @DisplayName("Deberia crear una cuenta existosamente")
    public void testCrearCuenta() {

        when(cuentaRepositorio.save(any(Cuenta.class))).thenAnswer(i -> {
            Cuenta u = i.getArgument(0);
            u.setFechaCuentaCreada(LocalDate.now());
            return u;
        });

        CuentaCrearReqDto dto = new CuentaCrearReqDto(
                "Gastos de la casa",
                "Cuenta para administrar los gastos de la casa",
                0.00);

        CuentaResDto cuentaPrueba = cuentaServicio.crearCuenta(dto);

        assertEquals("Gastos de la casa", cuentaPrueba.tituloCuenta());
        assertNotNull( cuentaPrueba.fechaCuentaCreada());
        assertEquals(0.00, cuentaPrueba.total());

        verify(this.cuentaRepositorio, times(1)).save(any(Cuenta.class));

    }

    @Test
    @DisplayName("Debe retornar una lista de CuentaResDto cuando hay cuentas registradas")
    void listarCuentas_conDatos_retornaListaMapeada() {

        Cuenta cuenta2 = new Cuenta();
        cuenta2.setId(2);
        cuenta2.setTituloCuenta("Cuenta Secundaria");
        cuenta2.setDescripcionCuenta("Cuenta de gastos");
        cuenta2.setTotal(500.0);
        cuenta2.setFechaCuentaCreada(LocalDate.of(2026, 2, 1));

        when(cuentaRepositorio.findAll()).thenReturn(List.of(cuenta, cuenta2));

        List<CuentaResDto> resultado = cuentaServicio.listarCuentas();

        assertThat(resultado.get(1).tituloCuenta()).isEqualTo("Cuenta Secundaria");
        assertThat(resultado.get(1).descripcionCuenta()).isEqualTo("Cuenta de gastos");
        assertThat(resultado.get(1).total()).isEqualTo(500.0);
        assertThat(resultado.get(1).fechaCuentaCreada()).isEqualTo(LocalDate.of(2026, 2, 1));

        assertThat(resultado.get(0).tituloCuenta()).isEqualTo("Gastos de la casa");

        verify(cuentaRepositorio, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar una lista vacía cuando no hay cuentas registradas")
    void listarCuentas_sinDatos_retornaListaVacia() {
        when(cuentaRepositorio.findAll()).thenReturn(List.of());

        List<CuentaResDto> resultado = cuentaServicio.listarCuentas();

        assertThat(resultado).isEmpty();
        verify(cuentaRepositorio, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe actualizar titulo y descripcion cuando ambos vienen informados")
    void actualizarCuenta_conAmbosCampos_actualizaTodo() {
        CuentaActualizarReqDto req = new CuentaActualizarReqDto(
                "Nuevo Titulo", "Nueva Descripcion");

        when(cuentaRepositorio.findById(1)).thenReturn(Optional.of(cuenta));

        CuentaActualizadaResDto resultado = cuentaServicio.actualizarCuenta(1, req);

        assertThat(resultado.tituloCuenta()).isEqualTo("Nuevo Titulo");
        assertThat(resultado.descripcionCuenta()).isEqualTo("Nueva Descripcion");
        assertThat(cuenta.getTituloCuenta()).isEqualTo("Nuevo Titulo");
        assertThat(cuenta.getDescripcionCuenta()).isEqualTo("Nueva Descripcion");

        verify(cuentaRepositorio, times(1)).findById(1);
    }

    @Test
    @DisplayName("Debe conservar el titulo original cuando viene null en el request")
    void actualizarCuenta_conTituloNull_noActualizaTitulo() {
        CuentaActualizarReqDto req = new CuentaActualizarReqDto(
                null, "Nueva Descripcion");

        when(cuentaRepositorio.findById(1)).thenReturn(Optional.of(cuenta));

        CuentaActualizadaResDto resultado = cuentaServicio.actualizarCuenta(1, req);

        assertThat(resultado.tituloCuenta()).isEqualTo("Gastos de la casa");
        assertThat(resultado.descripcionCuenta()).isEqualTo("Nueva Descripcion");
    }

    @Test
    @DisplayName("Debe conservar la descripcion original cuando viene null en el request")
    void actualizarCuenta_conDescripcionNull_noActualizaDescripcion() {
        CuentaActualizarReqDto req = new CuentaActualizarReqDto(
                "Nuevo Titulo", null);

        when(cuentaRepositorio.findById(1)).thenReturn(Optional.of(cuenta));

        CuentaActualizadaResDto resultado = cuentaServicio.actualizarCuenta(1, req);

        assertThat(resultado.tituloCuenta()).isEqualTo("Nuevo Titulo");
        assertThat(resultado.descripcionCuenta()).isEqualTo("Cuenta para administrar los gastos de la casa");

    }

    @Test
    @DisplayName("No debe modificar nada cuando ambos campos vienen null")
    void actualizarCuenta_conAmbosNull_noModificaNada() {
        CuentaActualizarReqDto req = new CuentaActualizarReqDto(null, null);

        when(cuentaRepositorio.findById(1)).thenReturn(Optional.of(cuenta));

        CuentaActualizadaResDto resultado = cuentaServicio.actualizarCuenta(1, req);

        assertThat(resultado.tituloCuenta()).isEqualTo("Gastos de la casa");
        assertThat(resultado.descripcionCuenta()).isEqualTo("Cuenta para administrar los gastos de la casa");
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando la cuenta no existe")
    void actualizarCuenta_idInexistente_lanzaExcepcion() {
        CuentaActualizarReqDto req = new CuentaActualizarReqDto("Titulo", "Descripcion");

        when(cuentaRepositorio.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cuentaServicio.actualizarCuenta(99, req))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");

        verify(cuentaRepositorio, never()).save(any());
    }

    @Test
    @DisplayName("Debe eliminar la cuenta cuando existe")
    void eliminarCuenta_idExistente_eliminaCorrectamente() {
        when(cuentaRepositorio.existsById(1)).thenReturn(true);

        cuentaServicio.eliminarCuenta(1);

        verify(cuentaRepositorio, times(1)).existsById(1);
        verify(cuentaRepositorio, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando la cuenta no existe")
    void eliminarCuenta_idInexistente_lanzaExcepcion() {
        when(cuentaRepositorio.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> cuentaServicio.eliminarCuenta(99))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");

        verify(cuentaRepositorio, never()).deleteById(anyInt());
    }

}
