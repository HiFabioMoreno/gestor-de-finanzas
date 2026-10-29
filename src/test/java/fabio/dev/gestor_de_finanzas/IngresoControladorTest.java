package fabio.dev.gestor_de_finanzas;

import fabio.dev.gestor_de_finanzas.controlador.IngresoControlador;
import fabio.dev.gestor_de_finanzas.dtos.IngresoActualizarReqDto;
import fabio.dev.gestor_de_finanzas.dtos.IngresoCrearReqDto;
import fabio.dev.gestor_de_finanzas.dtos.IngresoResDto;
import fabio.dev.gestor_de_finanzas.excepciones.GlobalExceptionHandler;
import fabio.dev.gestor_de_finanzas.excepciones.RecursoNoEncontradoException;
import fabio.dev.gestor_de_finanzas.modelos.Ingreso;
import fabio.dev.gestor_de_finanzas.servicios.IngresoServicio;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngresoControlador.class)
@Import(GlobalExceptionHandler.class)
public class IngresoControladorTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    IngresoServicio ingresoServicio;

    private IngresoResDto ingresoResDto;

    @BeforeEach
    void setup(){
        ingresoResDto = new IngresoResDto(
                1,
                "pago de proyecto",
                null,
                new BigDecimal(1500),
                LocalDate.now()
        );
    }

    @Test
    void encontrarIngresos_exitoso_debeRetornar200() throws  Exception {

        IngresoResDto ingresoResDtoDos = new IngresoResDto(
                1,
                "Pago de proyecto dos",
                null,
                new BigDecimal(1000),
                LocalDate.now()
        );

        when(ingresoServicio.buscarTodosIngresos()).thenReturn(List.of(ingresoResDto, ingresoResDtoDos));

        this.mvc.perform(get("/gestorFinanzas/ingresos/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tituloIngreso").value("pago de proyecto"))
                .andExpect(jsonPath("$[0].cantidadIngreso").value(1500))
                .andExpect(jsonPath("$[1].tituloIngreso").value("Pago de proyecto dos"))
                .andExpect(jsonPath("$[1].cantidadIngreso").value(1000));

    }


    @Test
    void encontrarIngresos_listaVacia_debeRetornar200() throws Exception {

        when(ingresoServicio.buscarTodosIngresos()).thenReturn(List.of());

        this.mvc.perform(get("/gestorFinanzas/ingresos/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void guardarIngreso_exitosamnete_debeRetornar200() throws Exception {

        when(this.ingresoServicio.agregarIngreso(any(IngresoCrearReqDto.class))).thenReturn(this.ingresoResDto);

        this.mvc.perform(post("/gestorFinanzas/ingresos/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(this.ingresoResDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tituloIngreso").value("pago de proyecto"))
                .andExpect(jsonPath("$.cantidadIngreso").value(new BigDecimal(1500)));

    }

    @Test
    void guardarIngreso_cantidadNegativa_debeRetornarBadRequest() throws Exception {

        IngresoCrearReqDto dto = new IngresoCrearReqDto(
                "Ingreso uno",
                null,
                new BigDecimal(-190000)
        );

        this.mvc.perform(post("/gestorFinanzas/ingresos/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

    }


    @Test
    void guardarIngreso_tituloNull_deberRetornarBadRequest() throws Exception {

        IngresoCrearReqDto dto = new IngresoCrearReqDto(
                null,
                null,
                new BigDecimal(1900)
        );

        this.mvc.perform(post("/gestorFinanzas/ingresos/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

    }

    @Test
    void actualizarIngreso_exitoso_deberRetornar200() throws Exception{

        IngresoActualizarReqDto req = new IngresoActualizarReqDto(
                "Pago de proyecto dos",
                null,
                new BigDecimal(1000)
        );

        IngresoResDto res = new IngresoResDto(
                1,
                "Pago de proyecto dos",
                null,
                new BigDecimal(1000),
                LocalDate.now()
        );

        when(this.ingresoServicio.actualizarIngreso(eq(1), any(IngresoActualizarReqDto.class))).thenReturn(res);

        this.mvc.perform(patch("/gestorFinanzas/ingresos/ingreso/1/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloIngreso").value("Pago de proyecto dos"))
                .andExpect(jsonPath("$.cantidadIngreso").value(1000));

    }

    @Test
    void actualizarIngreso_idInvalido_debeRetornarBadRequest() throws Exception {

        IngresoActualizarReqDto req = new IngresoActualizarReqDto(
                "Pago de proyecto dos",
                null,
                new BigDecimal(1000)
        );

        int id = -10;

        this.mvc.perform(patch("/gestorFinanzas/ingresos/ingreso/"+ id + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

    }

    @Test
    void actualizarIngreso_ingresoNoExistente_debeRetornarNotFound() throws Exception {

        IngresoActualizarReqDto req = new IngresoActualizarReqDto(
                "Pago de proyecto dos",
                null,
                new BigDecimal(1000)
        );

        int id = 100;

        doThrow(new RecursoNoEncontradoException("Ingreso no existe"))
                .when(this.ingresoServicio).actualizarIngreso(eq(id), any(IngresoActualizarReqDto.class));

        this.mvc.perform(patch("/gestorFinanzas/ingresos/ingreso/"+ id + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());

    }

    @Test
    void actualizaringreso_cantidadNegativa_debeRetornarBadRequest()  throws Exception {

        IngresoActualizarReqDto req = new IngresoActualizarReqDto(
                null,
                null,
                new BigDecimal(-1000)
        );

        int id = 1;

        this.mvc.perform(patch("/gestorFinanzas/ingresos/ingreso/"+ id + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

    }


    @Test
    void eliminarIngreso_existoso_debeRetornar204() throws Exception {

        int id = 1;
        this.mvc.perform(delete("/gestorFinanzas/ingresos/ingreso/"+ id + "/"))
                .andExpect(status().isNoContent());

        verify(this.ingresoServicio, times(1)).eliminarIngreso(id);

    }

    @Test
    void eliminarIngreso_idInvalido_debeRetornarBadRequest() throws Exception {

        int id = -10;

        this.mvc.perform(delete("/gestorFinanzas/ingresos/ingreso/"+ id + "/"))
                .andExpect(status().isBadRequest());

        verify(this.ingresoServicio,never()).eliminarIngreso(anyInt());

    }

    @Test
    void eliminarIngreso_ingresoNoExistente_debeRetornarNotFound() throws Exception{

        int id = 100;

        doThrow(new RecursoNoEncontradoException("Ingreso no existe"))
                .when(this.ingresoServicio).eliminarIngreso(id);

        this.mvc.perform(delete("/gestorFinanzas/ingresos/ingreso/"+ id + "/"))
                .andExpect(status().isNotFound());

    }


}