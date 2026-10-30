package fabio.dev.gestor_de_finanzas;

import fabio.dev.gestor_de_finanzas.dtos.IngresoActualizarReqDto;
import fabio.dev.gestor_de_finanzas.dtos.IngresoCrearReqDto;
import fabio.dev.gestor_de_finanzas.modelos.Ingreso;
import fabio.dev.gestor_de_finanzas.repositorios.IngresoRepositorio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:integrationdb;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class IngresoControladorIntegrationTest {

    private static final String BASE_URL = "/gestorFinanzas/ingresos/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IngresoRepositorio ingresoRepositorio;

    private Ingreso persistirIngreso(String titulo, String descripcion, BigDecimal cantidad) {
        Ingreso ingreso = new Ingreso();
        ingreso.setTituloIngreso(titulo);
        ingreso.setDescripcionIngreso(descripcion);
        ingreso.setCantidadIngreso(cantidad);
        return ingresoRepositorio.save(ingreso);
    }

    @Test
    @DisplayName("GET /gestorFinanzas/ingresos/ retorna lista vacía cuando no hay ingresos")
    void obtenerIngresos_sinDatos_retornaListaVacia() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /gestorFinanzas/ingresos/ retorna los ingresos persistidos")
    void obtenerIngresos_conDatos_retornaLista() throws Exception {
        persistirIngreso("Pago de proyecto", "Pago del proyecto uno", new BigDecimal("1500.00"));
        persistirIngreso("Pago de consultoría", null, new BigDecimal("1000.00"));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tituloIngreso").value("Pago de proyecto"))
                .andExpect(jsonPath("$[0].cantidadIngreso").value(1500.00))
                .andExpect(jsonPath("$[1].tituloIngreso").value("Pago de consultoría"));
    }

    @Test
    @DisplayName("POST /gestorFinanzas/ingresos/ crea el ingreso y lo persiste")
    void crearIngreso_datosValidos_retorna201YPersiste() throws Exception {
        IngresoCrearReqDto req = new IngresoCrearReqDto(
                "Pago de proyecto",
                "Pago del proyecto uno",
                new BigDecimal("1500.00")
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tituloIngreso").value("Pago de proyecto"))
                .andExpect(jsonPath("$.descripcionIngreso").value("Pago del proyecto uno"))
                .andExpect(jsonPath("$.cantidadIngreso").value(1500.00))
                .andExpect(jsonPath("$.fechaIngreso").isNotEmpty());

        assertThat(ingresoRepositorio.findAll()).hasSize(1);
        Ingreso persistido = ingresoRepositorio.findAll().get(0);
        assertThat(persistido.getTituloIngreso()).isEqualTo("Pago de proyecto");
        assertThat(persistido.getCantidadIngreso()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("POST /gestorFinanzas/ingresos/ con datos inválidos retorna 400")
    void crearIngreso_datosInvalidos_retorna400() throws Exception {
        IngresoCrearReqDto req = new IngresoCrearReqDto(null, null, new BigDecimal("-10.00"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        assertThat(ingresoRepositorio.findAll()).isEmpty();
    }

    @Test
    @DisplayName("PATCH /gestorFinanzas/ingresos/ingreso/{id}/ actualiza el ingreso persistido")
    void actualizarIngreso_datosValidos_retorna200YPersiste() throws Exception {
        Ingreso ingreso = persistirIngreso("Pago de proyecto", "Pago del proyecto uno", new BigDecimal("1500.00"));

        IngresoActualizarReqDto req = new IngresoActualizarReqDto(
                "Pago de consultoría",
                "Pago del proyecto dos",
                new BigDecimal("1000.00")
        );

        mockMvc.perform(patch(BASE_URL + "ingreso/" + ingreso.getId() + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloIngreso").value("Pago de consultoría"))
                .andExpect(jsonPath("$.descripcionIngreso").value("Pago del proyecto dos"))
                .andExpect(jsonPath("$.cantidadIngreso").value(1000.00));

        Ingreso actualizado = ingresoRepositorio.findById(ingreso.getId()).orElseThrow();
        assertThat(actualizado.getTituloIngreso()).isEqualTo("Pago de consultoría");
        assertThat(actualizado.getDescripcionIngreso()).isEqualTo("Pago del proyecto dos");
        assertThat(actualizado.getCantidadIngreso()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("PATCH /gestorFinanzas/ingresos/ingreso/{id}/ con id inexistente retorna 404")
    void actualizarIngreso_idInexistente_retorna404() throws Exception {
        IngresoActualizarReqDto req = new IngresoActualizarReqDto("Pago de consultoría", null, new BigDecimal("1000.00"));

        mockMvc.perform(patch(BASE_URL + "ingreso/9999/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /gestorFinanzas/ingresos/ingreso/{id}/ con id no positivo retorna 400")
    void actualizarIngreso_idNoPositivo_retorna400() throws Exception {
        IngresoActualizarReqDto req = new IngresoActualizarReqDto("Pago de consultoría", null, new BigDecimal("1000.00"));

        mockMvc.perform(patch(BASE_URL + "ingreso/0/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /gestorFinanzas/ingresos/ingreso/{id}/ elimina el ingreso persistido")
    void eliminarIngreso_idExistente_retorna204YElimina() throws Exception {
        Ingreso ingreso = persistirIngreso("Pago de proyecto", "Pago del proyecto uno", new BigDecimal("1500.00"));

        mockMvc.perform(delete(BASE_URL + "ingreso/" + ingreso.getId() + "/"))
                .andExpect(status().isNoContent());

        assertThat(ingresoRepositorio.existsById(ingreso.getId())).isFalse();
    }

    @Test
    @DisplayName("DELETE /gestorFinanzas/ingresos/ingreso/{id}/ con id inexistente retorna 404")
    void eliminarIngreso_idInexistente_retorna404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "ingreso/9999/"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /gestorFinanzas/ingresos/ingreso/{id}/ con id no positivo retorna 400")
    void eliminarIngreso_idNoPositivo_retorna400() throws Exception {
        mockMvc.perform(delete(BASE_URL + "ingreso/0/"))
                .andExpect(status().isBadRequest());
    }
}
