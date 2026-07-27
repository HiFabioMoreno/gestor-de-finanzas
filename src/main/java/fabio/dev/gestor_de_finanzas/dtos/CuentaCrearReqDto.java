package fabio.dev.gestor_de_finanzas.dtos;

public record CuentaCrearReqDto(
        String tituloCuenta,
        String descripcionCuenta,
        Double total
) {
}
