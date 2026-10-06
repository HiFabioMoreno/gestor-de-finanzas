package fabio.dev.gestor_de_finanzas;

import tools.jackson.databind.ObjectMapper;
import fabio.dev.gestor_de_finanzas.controlador.CuentaControlador;
import fabio.dev.gestor_de_finanzas.dtos.CuentaActualizadaResDto;
import fabio.dev.gestor_de_finanzas.dtos.CuentaActualizarReqDto;
import fabio.dev.gestor_de_finanzas.dtos.CuentaCrearReqDto;
import fabio.dev.gestor_de_finanzas.dtos.CuentaResDto;
import fabio.dev.gestor_de_finanzas.excepciones.GlobalExceptionHandler;
import fabio.dev.gestor_de_finanzas.excepciones.RecursoNoEncontradoException;
import fabio.dev.gestor_de_finanzas.servicios.CuentaServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CuentaControlador.class)
@Import(GlobalExceptionHandler.class)
public class CuentaControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CuentaServicio cuentaServicio;

    private CuentaResDto cuentaResDto;

    @BeforeEach
    void setUp() {
        cuentaResDto = new CuentaResDto(
                "Gastos de la casa",
                "Cuenta para administrar los gastos de la casa",
                0.00,
                LocalDate.now()
        );
    }

    @Test
    @DisplayName("Debería listar todas las cuentas correctamente")
    void obtenerCuentas_debeRetornarLista() throws Exception {
        CuentaResDto cuenta2 = new CuentaResDto(
                "Cuenta Secundaria",
                "Cuenta de gastos",
                500.0,
                LocalDate.of(2026, 2, 1)
        );
        when(cuentaServicio.listarCuentas()).thenReturn(List.of(cuentaResDto, cuenta2));

        mockMvc.perform(get("/gestorFinanzas/cuenta/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tituloCuenta").value("Gastos de la casa"))
                .andExpect(jsonPath("$[1].tituloCuenta").value("Cuenta Secundaria"));
    }

    @Test
    @DisplayName("Debería listar cuentas vacías cuando no hay cuentas")
    void obtenerCuentas_sinDatos_debeRetornarListaVacia() throws Exception {
        when(cuentaServicio.listarCuentas()).thenReturn(List.of());

        mockMvc.perform(get("/gestorFinanzas/cuenta/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Debería crear una cuenta correctamente")
    void crearCuenta_debeCrearYRetornar201() throws Exception {
        CuentaCrearReqDto req = new CuentaCrearReqDto(
                "Gastos de la casa",
                "Cuenta para administrar los gastos de la casa",
                0.00
        );
        when(cuentaServicio.crearCuenta(any(CuentaCrearReqDto.class))).thenReturn(cuentaResDto);

        mockMvc.perform(post("/gestorFinanzas/cuenta/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tituloCuenta").value("Gastos de la casa"))
                .andExpect(jsonPath("$.descripcionCuenta").value("Cuenta para administrar los gastos de la casa"));
    }

    @Test
    @DisplayName("No debería crear cuenta con datos inválidos - BAD_REQUEST")
    void crearCuenta_datosInvalidos_debeRetornarBadRequest() throws Exception {
        CuentaCrearReqDto req = new CuentaCrearReqDto("", "", -10.0);

        mockMvc.perform(post("/gestorFinanzas/cuenta/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debería actualizar una cuenta correctamente")
    void actualizarCuenta_debeActualizarYRetornar200() throws Exception {
        CuentaActualizarReqDto req = new CuentaActualizarReqDto("Nuevo Titulo", "Nueva Descripcion");
        CuentaActualizadaResDto res = new CuentaActualizadaResDto("Nuevo Titulo", "Nueva Descripcion");
        when(cuentaServicio.actualizarCuenta(eq(1), any(CuentaActualizarReqDto.class))).thenReturn(res);

        mockMvc.perform(patch("/gestorFinanzas/cuenta/1/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloCuenta").value("Nuevo Titulo"))
                .andExpect(jsonPath("$.descripcionCuenta").value("Nueva Descripcion"));
    }

    @Test
    @DisplayName("No debería actualizar cuenta con ID no positivo - BAD_REQUEST")
    void actualizarCuenta_idNoPositivo_debeRetornarBadRequest() throws Exception {
        CuentaActualizarReqDto req = new CuentaActualizarReqDto("Nuevo Titulo", "Nueva Descripcion");

        mockMvc.perform(patch("/gestorFinanzas/cuenta/0/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("No debería actualizar cuenta con datos inválidos - BAD_REQUEST")
    void actualizarCuenta_datosInvalidos_debeRetornarBadRequest() throws Exception {
        CuentaActualizarReqDto req = new CuentaActualizarReqDto("", "Nueva Descripcion");

        mockMvc.perform(patch("/gestorFinanzas/cuenta/1/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debería retornar 404 cuando actualiza cuenta inexistente")
    void actualizarCuenta_noEncontrado_debeRetornarNotFound() throws Exception {
        CuentaActualizarReqDto req = new CuentaActualizarReqDto("Nuevo Titulo", "Nueva Descripcion");
        doThrow(new RecursoNoEncontradoException("No existe la cuenta con id: 99"))
                .when(cuentaServicio).actualizarCuenta(eq(99), any(CuentaActualizarReqDto.class));

        mockMvc.perform(patch("/gestorFinanzas/cuenta/99/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debería eliminar una cuenta correctamente")
    void eliminarCuenta_debeRetornar204() throws Exception {
        mockMvc.perform(delete("/gestorFinanzas/cuenta/1/"))
                .andExpect(status().isNoContent());
        verify(cuentaServicio).eliminarCuenta(1);
    }

    @Test
    @DisplayName("No debería eliminar cuenta con ID no positivo - BAD_REQUEST")
    void eliminarCuenta_idNoPositivo_debeRetornarBadRequest() throws Exception {
        mockMvc.perform(delete("/gestorFinanzas/cuenta/0/"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debería retornar 404 cuando elimina cuenta inexistente")
    void eliminarCuenta_noEncontrado_debeRetornarNotFound() throws Exception {
        doThrow(new RecursoNoEncontradoException("No existe el cuenta con el id: 99"))
                .when(cuentaServicio).eliminarCuenta(99);

        mockMvc.perform(delete("/gestorFinanzas/cuenta/99/"))
                .andExpect(status().isNotFound());
    }
}
