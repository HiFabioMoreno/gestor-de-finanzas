package fabio.dev.gestor_de_finanzas;

import fabio.dev.gestor_de_finanzas.dtos.CuentaActualizarReqDto;
import fabio.dev.gestor_de_finanzas.dtos.CuentaCrearReqDto;
import fabio.dev.gestor_de_finanzas.modelos.Cuenta;
import fabio.dev.gestor_de_finanzas.repositorios.CuentaRepositorio;
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
class CuentaControladorIntegrationTest {

    private static final String BASE_URL = "/gestorFinanzas/cuenta/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CuentaRepositorio cuentaRepositorio;

    private Cuenta persistirCuenta(String titulo, String descripcion, BigDecimal total) {
        Cuenta cuenta = new Cuenta();
        cuenta.setTituloCuenta(titulo);
        cuenta.setDescripcionCuenta(descripcion);
        cuenta.setTotal(total);
        return cuentaRepositorio.save(cuenta);
    }

    @Test
    @DisplayName("GET /gestorFinanzas/cuenta/ retorna lista vacía cuando no hay cuentas")
    void obtenerCuentas_sinDatos_retornaListaVacia() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /gestorFinanzas/cuenta/ retorna las cuentas persistidas")
    void obtenerCuentas_conDatos_retornaLista() throws Exception {
        persistirCuenta("Gastos de la casa", "Cuenta principal", new BigDecimal("1000.00"));
        persistirCuenta("Ahorros personales", "Cuenta de ahorro", new BigDecimal("5000.00"));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tituloCuenta").value("Gastos de la casa"))
                .andExpect(jsonPath("$[1].tituloCuenta").value("Ahorros personales"));
    }

    @Test
    @DisplayName("POST /gestorFinanzas/cuenta/ crea la cuenta y la persiste")
    void crearCuenta_datosValidos_retorna201YPersiste() throws Exception {
        CuentaCrearReqDto req = new CuentaCrearReqDto(
                "Gastos de la casa",
                "Cuenta para administrar los gastos de la casa",
                new BigDecimal("1500.00")
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tituloCuenta").value("Gastos de la casa"))
                .andExpect(jsonPath("$.descripcionCuenta").value("Cuenta para administrar los gastos de la casa"))
                .andExpect(jsonPath("$.total").value(1500.00))
                .andExpect(jsonPath("$.fechaCuentaCreada").isNotEmpty());

        assertThat(cuentaRepositorio.findAll()).hasSize(1);
        assertThat(cuentaRepositorio.findAll().get(0).getTituloCuenta()).isEqualTo("Gastos de la casa");
    }

    @Test
    @DisplayName("POST /gestorFinanzas/cuenta/ con datos inválidos retorna 400")
    void crearCuenta_datosInvalidos_retorna400() throws Exception {
        CuentaCrearReqDto req = new CuentaCrearReqDto("", null, new BigDecimal("-10.00"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        assertThat(cuentaRepositorio.findAll()).isEmpty();
    }

    @Test
    @DisplayName("PATCH /gestorFinanzas/cuenta/{id}/ actualiza la cuenta persistida")
    void actualizarCuenta_datosValidos_retorna200YPersiste() throws Exception {
        Cuenta cuenta = persistirCuenta(
                "Gastos de la casa",
                "Cuenta para administrar los gastos de la casa",
                new BigDecimal("1000.00"));

        CuentaActualizarReqDto req = new CuentaActualizarReqDto(
                "Gastos del hogar",
                "Nueva descripción de la cuenta"
        );

        mockMvc.perform(patch(BASE_URL + cuenta.getId() + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloCuenta").value("Gastos del hogar"))
                .andExpect(jsonPath("$.descripcionCuenta").value("Nueva descripción de la cuenta"));

        Cuenta actualizada = cuentaRepositorio.findById(cuenta.getId()).orElseThrow();
        assertThat(actualizada.getTituloCuenta()).isEqualTo("Gastos del hogar");
        assertThat(actualizada.getDescripcionCuenta()).isEqualTo("Nueva descripción de la cuenta");
    }

    @Test
    @DisplayName("PATCH /gestorFinanzas/cuenta/{id}/ con id inexistente retorna 404")
    void actualizarCuenta_idInexistente_retorna404() throws Exception {
        CuentaActualizarReqDto req = new CuentaActualizarReqDto("Gastos del hogar", "Descripción");

        mockMvc.perform(patch(BASE_URL + "9999/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /gestorFinanzas/cuenta/{id}/ con id no positivo retorna 400")
    void actualizarCuenta_idNoPositivo_retorna400() throws Exception {
        CuentaActualizarReqDto req = new CuentaActualizarReqDto("Gastos del hogar", "Descripción");

        mockMvc.perform(patch(BASE_URL + "0/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /gestorFinanzas/cuenta/{id}/ elimina la cuenta persistida")
    void eliminarCuenta_idExistente_retorna204YElimina() throws Exception {
        Cuenta cuenta = persistirCuenta(
                "Gastos de la casa",
                "Cuenta para administrar los gastos de la casa",
                new BigDecimal("1000.00"));

        mockMvc.perform(delete(BASE_URL + cuenta.getId() + "/"))
                .andExpect(status().isNoContent());

        assertThat(cuentaRepositorio.existsById(cuenta.getId())).isFalse();
    }

    @Test
    @DisplayName("DELETE /gestorFinanzas/cuenta/{id}/ con id inexistente retorna 404")
    void eliminarCuenta_idInexistente_retorna404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "9999/"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /gestorFinanzas/cuenta/{id}/ con id no positivo retorna 400")
    void eliminarCuenta_idNoPositivo_retorna400() throws Exception {
        mockMvc.perform(delete(BASE_URL + "0/"))
                .andExpect(status().isBadRequest());
    }
}
