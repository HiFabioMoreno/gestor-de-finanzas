package fabio.dev.gestor_de_finanzas.modelos;


import fabio.dev.gestor_de_finanzas.utilitis.Utilitis;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El titulo de la cuenta es necesario, por lo cual no debe estar vacío")
    @Size(min = 10, message = "El titulo de la cuenta debe ser mayor a 10 caracteres")
    @Column(nullable = false, unique = true, length = 220)
    private String tituloCuenta;

    @Column(length = 250)
    private String descripcionCuenta;

    @OneToMany(mappedBy = "cuenta", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingreso> ingresos = new ArrayList<>();

    @OneToMany(mappedBy = "cuenta", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Gasto> gastos = new ArrayList<>();

    @NotNull(message = "El total de la cuenta es necesario, por lo cual no debe estar vacío")
    @Positive(message = "El total de la cuenta debe ser mayor a cero")
    private Double total;

    @NotNull(message = "La fecha de la creacion de la cuenta no debe estar vacia")
    private LocalDate fechaCuentaCreada;

    @PrePersist
    public void prePersist() {
        this.fechaCuentaCreada = Utilitis.GenerarFecha();
    }

    public void agregarGasto(Gasto gasto) {
        gastos.add(gasto);
        gasto.setCuenta(this);
    }

    public void removerGasto(Gasto gasto) {
        gastos.remove(gasto);
        gasto.setCuenta(null);
    }

    public void agregarIngreso(Ingreso ingreso) {
        ingresos.add(ingreso);
        ingreso.setCuenta(this);
    }

    public void removerIngreso(Ingreso ingreso) {
        ingresos.remove(ingreso);
        ingreso.setCuenta(null);
    }
}