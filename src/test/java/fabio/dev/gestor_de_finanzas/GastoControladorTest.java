package fabio.dev.gestor_de_finanzas;

import fabio.dev.gestor_de_finanzas.controlador.GastoControlador;
import fabio.dev.gestor_de_finanzas.dtos.CuentaActualizarReqDto;
import fabio.dev.gestor_de_finanzas.dtos.GastoActualizarReqDto;
import fabio.dev.gestor_de_finanzas.dtos.GastoCrearReqDto;
import fabio.dev.gestor_de_finanzas.dtos.GastoResDto;
import fabio.dev.gestor_de_finanzas.excepciones.GlobalExceptionHandler;
import fabio.dev.gestor_de_finanzas.excepciones.RecursoNoEncontradoException;
import fabio.dev.gestor_de_finanzas.modelos.CategoriasPredefinidas;
import fabio.dev.gestor_de_finanzas.servicios.GastoServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GastoControlador.class)
@Import(GlobalExceptionHandler.class)
public class GastoControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GastoServicio gastoServicio;

    private GastoResDto gastoResDto;

    @BeforeEach()
    void setup(){
        this.gastoResDto = new GastoResDto(
                1,
                "Tenis Nike",
                "Compra de tenis para el gym",
                Arrays.asList(CategoriasPredefinidas.OCIO),
                new BigDecimal("2100.00"),
                LocalDate.now()
        );
    }

    @Test
    @DisplayName("Debería listar todas los gastos correctamente")
    void  obtenerGastos_deberRetornarLista() throws Exception {

        GastoResDto gastoDos = new GastoResDto(
                1,
                "Medicamento Ibuprofeno",
                null,
                List.of(CategoriasPredefinidas.SALUD),
                new BigDecimal("120.00"),
                LocalDate.now()
        );

        when(gastoServicio.buscarTodosGastos()).thenReturn(List.of(this.gastoResDto, gastoDos));

        mockMvc.perform(get("/gestorFinanzas/gastos/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tituloGasto").value("Tenis Nike"))
                .andExpect(jsonPath("$[1].tituloGasto").value("Medicamento Ibuprofeno"));

    }

    @Test
    @DisplayName("Deberia listar cuentas vacias cuando no hay gastos")
    void obtenerGastos_deberiaRetornarListaVacia()throws Exception {

        when(gastoServicio.buscarTodosGastos()).thenReturn(List.of());

        mockMvc.perform(get("/gestorFinanzas/gastos/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

    }

    @Test
    @DisplayName("Deberia crear un gasto exitosamente")
    void crearGasto_debeCrearYRetornar201() throws Exception {

        when(this.gastoServicio.guardarGasto(any(GastoCrearReqDto.class))).thenReturn(this.gastoResDto);

        mockMvc.perform(post("/gestorFinanzas/gastos/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(this.gastoResDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tituloGasto").value("Tenis Nike"))
                .andExpect(jsonPath("$.cantidad").value(2100.00));
    }

    @Test
    @DisplayName("No debería crear un gasto con datos inválidos - BAD_REQUEST")
    void crearGasto_datosInvalidos_debeRetornarBadRequest() throws Exception {

        GastoCrearReqDto req = new GastoCrearReqDto (
                "",
                "",
                null,
                new BigDecimal("-10.0"));

        mockMvc.perform(post("/gestorFinanzas/gastos/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("Deberia actualizar un gasto exitosamente")
    void actualizarGasto_debeActualizarYRetornar200() throws Exception {

        GastoActualizarReqDto req = new GastoActualizarReqDto(
                "Compra de tenis asics",
                null,
                null,
                new BigDecimal("2600.00"));

        GastoResDto res = new GastoResDto(1,
                "Compra de tenis asics",
                gastoResDto.descripcionGasto(), gastoResDto.categorias(), new BigDecimal("2600.00"), gastoResDto.fechaGasto());

        when(gastoServicio.actualizarGasto(1, req)).thenReturn(res);

        Integer idGasto = 1;

        when(gastoServicio.actualizarGasto(eq(idGasto), any(GastoActualizarReqDto.class))).thenReturn(res);


        mockMvc.perform(patch("/gestorFinanzas/gastos/gasto/" + idGasto + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloGasto").value("Compra de tenis asics"))
                .andExpect(jsonPath("$.cantidad").value(2600.00));

    }

    @Test
    @DisplayName("No deberia actualizar un gasto por id invalido")
    void actualizarGasto_idNoPositivo_debeRetornarBadRequest() throws Exception {

        int idInvalido = -100;

        GastoActualizarReqDto req = new GastoActualizarReqDto (
                "",
                "",
                null,
                new BigDecimal("1000.0"));

        mockMvc.perform(patch("/gestorFinanzas/gastos/gasto/"+ idInvalido +"/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("No deberia actualizar un gasto por tipo de dato invalido")
    void actualizarGasto_noEncontrado_debeRetornarNotFound() throws Exception{
        int idInvalido = 100;

        GastoActualizarReqDto req = new GastoActualizarReqDto (
                "",
                "",
                null,
                new BigDecimal("1000.0"));

        doThrow(new RecursoNoEncontradoException("No existe gasto con id:" + idInvalido))
                .when(gastoServicio).actualizarGasto(eq(idInvalido), any(GastoActualizarReqDto.class));

        mockMvc.perform(patch("/gestorFinanzas/gastos/gasto/"+ idInvalido +"/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deberia eliminar un gasto exitosamente")
    void eliminarGasto_debeEliminarYRetornar204()throws Exception{

        mockMvc.perform(delete("/gestorFinanzas/gastos/gasto/1/"))
                .andExpect(status().isNoContent());

        verify(gastoServicio).eliminarGasto(1);

    }


    @Test
    @DisplayName("No debería eliminar cuenta con ID no positivo - BAD_REQUEST")
    void eliminarGasto_idNoPositivo_debeRetornarBadRequest() throws Exception {

        mockMvc.perform(patch("/gestorFinanzas/gastos/gasto/"+ 0 +"/"))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("Debería retornar 404 cuando elimina cuenta inexistente")
    void eliminarCuenta_noEncontrado_debeRetornarNotFound() throws Exception {
        doThrow(new RecursoNoEncontradoException("No existe el gasto con el id: 99"))
                .when(gastoServicio).eliminarGasto(99);

        mockMvc.perform(delete("/gestorFinanzas/gastos/gasto/99/"))
                .andExpect(status().isNotFound());
    }


}
