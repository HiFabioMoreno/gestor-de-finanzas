package fabio.dev.gestor_de_finanzas;

import fabio.dev.gestor_de_finanzas.dtos.GastoActualizarReqDto;
import fabio.dev.gestor_de_finanzas.dtos.GastoCrearReqDto;
import fabio.dev.gestor_de_finanzas.modelos.CategoriasPredefinidas;
import fabio.dev.gestor_de_finanzas.modelos.Gasto;
import fabio.dev.gestor_de_finanzas.repositorios.GastoRepositorio;
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
import java.util.List;

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
class GastoControladorIntegrationTest {

    private static final String BASE_URL = "/gestorFinanzas/gastos/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GastoRepositorio gastoRepositorio;

    private Gasto persistirGasto(String titulo, String descripcion, List<CategoriasPredefinidas> categorias, BigDecimal cantidad) {
        Gasto gasto = new Gasto();
        gasto.setTituloGasto(titulo);
        gasto.setDescripcionGasto(descripcion);
        gasto.setCategorias(categorias);
        gasto.setCantidad(cantidad);
        return gastoRepositorio.save(gasto);
    }

    @Test
    @DisplayName("GET /gestorFinanzas/gastos/ retorna lista vacía cuando no hay gastos")
    void obtenerGastos_sinDatos_retornaListaVacia() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /gestorFinanzas/gastos/ retorna los gastos persistidos")
    void obtenerGastos_conDatos_retornaLista() throws Exception {
        persistirGasto("Tenis Nike", "Compra de tenis para el gym", List.of(CategoriasPredefinidas.OCIO), new BigDecimal("2100.00"));
        persistirGasto("Medicamento Ibuprofeno", null, List.of(CategoriasPredefinidas.SALUD), new BigDecimal("120.00"));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tituloGasto").value("Tenis Nike"))
                .andExpect(jsonPath("$[0].categorias[0]").value("OCIO"))
                .andExpect(jsonPath("$[1].tituloGasto").value("Medicamento Ibuprofeno"));
    }

    @Test
    @DisplayName("POST /gestorFinanzas/gastos/ crea el gasto y lo persiste")
    void crearGasto_datosValidos_retorna201YPersiste() throws Exception {
        GastoCrearReqDto req = new GastoCrearReqDto(
                "Tenis Nike",
                "Compra de tenis para el gym",
                List.of(CategoriasPredefinidas.OCIO),
                new BigDecimal("2100.00")
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tituloGasto").value("Tenis Nike"))
                .andExpect(jsonPath("$.cantidad").value(2100.00))
                .andExpect(jsonPath("$.categorias[0]").value("OCIO"))
                .andExpect(jsonPath("$.fechaGasto").isNotEmpty());

        assertThat(gastoRepositorio.findAll()).hasSize(1);
        Gasto persistido = gastoRepositorio.findAll().get(0);
        assertThat(persistido.getTituloGasto()).isEqualTo("Tenis Nike");
        assertThat(persistido.getCantidad()).isEqualByComparingTo("2100.00");
        assertThat(persistido.getCategorias()).containsExactly(CategoriasPredefinidas.OCIO);
    }

    @Test
    @DisplayName("POST /gestorFinanzas/gastos/ con datos inválidos retorna 400")
    void crearGasto_datosInvalidos_retorna400() throws Exception {
        GastoCrearReqDto req = new GastoCrearReqDto("", "", null, new BigDecimal("-10.00"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        assertThat(gastoRepositorio.findAll()).isEmpty();
    }

    @Test
    @DisplayName("PATCH /gestorFinanzas/gastos/gasto/{id}/ actualiza el gasto persistido")
    void actualizarGasto_datosValidos_retorna200YPersiste() throws Exception {
        Gasto gasto = persistirGasto(
                "Tenis Nike",
                "Compra de tenis para el gym",
                List.of(CategoriasPredefinidas.OCIO),
                new BigDecimal("2100.00"));

        GastoActualizarReqDto req = new GastoActualizarReqDto(
                "Tenis Asics",
                "Compra de tenis Asics",
                List.of(CategoriasPredefinidas.SALUD),
                new BigDecimal("2600.00")
        );

        mockMvc.perform(patch(BASE_URL + "gasto/" + gasto.getId() + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloGasto").value("Tenis Asics"))
                .andExpect(jsonPath("$.cantidad").value(2600.00))
                .andExpect(jsonPath("$.categorias[0]").value("SALUD"));

        Gasto actualizado = gastoRepositorio.findById(gasto.getId()).orElseThrow();
        assertThat(actualizado.getTituloGasto()).isEqualTo("Tenis Asics");
        assertThat(actualizado.getCantidad()).isEqualByComparingTo("2600.00");
        assertThat(actualizado.getCategorias()).containsExactly(CategoriasPredefinidas.SALUD);
    }

    @Test
    @DisplayName("PATCH /gestorFinanzas/gastos/gasto/{id}/ con id inexistente retorna 404")
    void actualizarGasto_idInexistente_retorna404() throws Exception {
        GastoActualizarReqDto req = new GastoActualizarReqDto("Tenis Asics", null, null, new BigDecimal("2600.00"));

        mockMvc.perform(patch(BASE_URL + "gasto/9999/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /gestorFinanzas/gastos/gasto/{id}/ con id no positivo retorna 400")
    void actualizarGasto_idNoPositivo_retorna400() throws Exception {
        GastoActualizarReqDto req = new GastoActualizarReqDto("Tenis Asics", null, null, new BigDecimal("2600.00"));

        mockMvc.perform(patch(BASE_URL + "gasto/0/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /gestorFinanzas/gastos/gasto/{id}/ elimina el gasto persistido")
    void eliminarGasto_idExistente_retorna204YElimina() throws Exception {
        Gasto gasto = persistirGasto(
                "Tenis Nike",
                "Compra de tenis para el gym",
                List.of(CategoriasPredefinidas.OCIO),
                new BigDecimal("2100.00"));

        mockMvc.perform(delete(BASE_URL + "gasto/" + gasto.getId() + "/"))
                .andExpect(status().isNoContent());

        assertThat(gastoRepositorio.existsById(gasto.getId())).isFalse();
    }

    @Test
    @DisplayName("DELETE /gestorFinanzas/gastos/gasto/{id}/ con id inexistente retorna 404")
    void eliminarGasto_idInexistente_retorna404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "gasto/9999/"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /gestorFinanzas/gastos/gasto/{id}/ con id no positivo retorna 400")
    void eliminarGasto_idNoPositivo_retorna400() throws Exception {
        mockMvc.perform(delete(BASE_URL + "gasto/0/"))
                .andExpect(status().isBadRequest());
    }
}
